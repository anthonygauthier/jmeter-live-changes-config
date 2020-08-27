package io.github.delirius325.jmeter.config.livechanges;

import io.github.delirius325.jmeter.config.livechanges.api.App;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

/**
 * Class that contains executes all the logic for the REST API to communicate with JMeter
 */
public class LiveChanges extends ConfigTestElement implements TestBean, LoopIterationListener, TestStateListener, SampleListener, ThreadListener {
    private static final String testPlanFile = String.format("%s/%s", FileServer.getFileServer().getBaseDir(), FileServer.getFileServer().getScriptName());
    private static final Logger logger = LoggerFactory.getLogger(LiveChanges.class);

    // Class attributes
    private App app;
    private Integer httpServerPort;

    // Static Attributes - available to other classes
    private static int staticCalcRate;
    private static int activeThreads;
    private static JMeterVariables jMeterVariables;
    private static Properties jMeterProperties;
    private static HashSet<ThreadGroup> testThreadGroups = new HashSet<>();
    private static ThreadGroup activeThreadGroup = new ThreadGroup();
    private static boolean stopTest;
    private static HashTree testPlanTree;
    private static StandardJMeterEngine jMeterEngine;
    private static SamplerMap samplerMap = new SamplerMap();
    private static boolean stopThreadsFromAPI;

    /**
     * Method that is executed when the test has started
     */
    @Override
    public void testStarted() { this.startServer(); }

    /**
     * Method that is executed when the test has started on a remote host
     * @param host String
     */
    @Override
    public void testStarted(String host) { }

    /**
     * Method that is executed when the test has ended
     */
    @Override
    public void testEnded() {
        try {
            this.finalizeTest();
        } catch (Exception e) {
            logger.error("LiveChanges API was unable to gracefully shutdown. More info in JMeter's console.");
        }
    }

    /**
     * Method that is executed when the test has ended on a remote host
     * @param host String
     */
    @Override
    public void testEnded(String host) { }

    /**
     * Method that is executed upon every thread iteration of the JMeter script
     * This plugin works specifically because of this method that acts as an non-blocking event loop
     * @param event LoopIterationEvent
     */
    @Override
    public void iterationStart(LoopIterationEvent event) {
        if(event.getIteration() == 1) {
            jMeterEngine = JMeterContextService.getContext().getEngine();
        }

        ThreadGroup threadGroup = (ThreadGroup) JMeterContextService.getContext().getThreadGroup();
        JMeterVariables vars = JMeterContextService.getContext().getVariables();
        Properties props = JMeterContextService.getContext().getProperties();
        String tmp = JMeterContextService.getContext().getThread().getThreadName();
        String threadName = tmp.substring(0, tmp.lastIndexOf("-"));
        getTestThreadGroups().add(threadGroup);

        if(stopTest) {
            jMeterEngine.askThreadsToStop();
        }

        checkForThreadChanges(threadGroup, event, threadName);
        this.checkForVariableChanges(vars, event);
        this.checkForPropertyChanges(props, event);
    }

    @Override
    public void sampleStopped(SampleEvent sampleEvent) { }

    @Override
    public void sampleStarted(SampleEvent sampleEvent) { }

    @Override
    public void sampleOccurred(SampleEvent sampleEvent) {
        samplerMap.add(sampleEvent.getResult());
    }

    public void threadStarted() { }

    public void threadFinished() {
        // Doing this helps avoid weird errors from stopping the test within the GUI.
        // This is due to the fact that JMeter isn't necessarily meant to stop/start thread on-the-go all the time
        if (stopThreadsFromAPI) {
            JMeterContextService.addTotalThreads(-1);
        }
        stopThreadsFromAPI = false;
    }

    /**
     * Method that checks for changes to the thread groups
     * @param threadGroup ThreadGroup
     * @param event LoopIterationEvent
     * @param threadName String
     */
    public static void checkForThreadChanges(ThreadGroup threadGroup, LoopIterationEvent event, String threadName) {
        // this if condition is necessary to verify if we are dealing with the correct thread group
        if(threadGroup.getName().equals(activeThreadGroup.getName())) {
            if(activeThreads != threadGroup.numberOfActiveThreads() && event.getIteration() != 1) {
                int diff = threadGroup.numberOfActiveThreads() - activeThreads;

                if(diff != 0) {
                    for(int i=0; i < Math.abs(diff); i++) {
                        if(diff < 0) {
                            threadGroup.addNewThread(0, jMeterEngine);
                        } else {
                            threadGroup.stopThread(threadName + "-" + threadGroup.numberOfActiveThreads(), true);
                            stopThreadsFromAPI = true;
                        }
                    }
                } else {
                    // gracefully stop threads
                    threadGroup.stop();
                    threadGroup.waitThreadsStopped();
                }
            } else {
                activeThreads = threadGroup.numberOfActiveThreads();
            }
        }
    }

    /**
     * Method that checks for changes to the user defined variables
     * @param vars JMeterVariables
     * @param event LoopIterationEvent
     */
    public void checkForVariableChanges(JMeterVariables vars, LoopIterationEvent event) {
        if(event.getIteration() == 1) {
            jMeterVariables = vars;
        } else {
            JMeterContextService.getContext().setVariables(jMeterVariables);
        }
    }

    /**
     * Method that checks for changes to the properties
     * @param props Properties
     * @param event LoopIterationEvent
     */
    public void checkForPropertyChanges(Properties props, LoopIterationEvent event) {
        if(event.getIteration() == 1) {
            jMeterProperties = props;
        }
    }

    /**
     * Method that initiate the server and sets attributes (testPlanTree, stopTest)
     */
    private void startServer() {
        try {
            testPlanTree = SaveService.loadTree(new File(testPlanFile));
            stopTest = false;
            this.app = new App(this.httpServerPort);
            this.app.start();
        } catch (IOException e) {
            logger.error("LiveChanges was unable to load the test plan tree. More info in JMeter's console.");
            e.printStackTrace();
        }
    }

    /**
     * Method that gracefully shutdowns the server
     */
    private void finalizeTest() {
        try {
            jMeterEngine.stopTest(true);
            this.app.stop();
            logger.info("LiveChanges API was successfully stopped.");
        } catch (Exception e) {
            logger.error("LiveChanges was unable to correctly shutdown Jetty server. More info in JMeter's console", e);
        }
    }

    /**
     * Getters / Setters
     */
    public int getHttpServerPort() {
        return this.httpServerPort;
    }
    public void setHttpServerPort(int port) {
        this.httpServerPort = port;
    }
    public static int getActiveThreads() {
        return activeThreads;
    }
    public static SamplerMap getSamplerMap() { return samplerMap; }
    public static StandardJMeterEngine getjMeterEngine() { return jMeterEngine; }
    public static ThreadGroup getActiveThreadGroup() { return activeThreadGroup; }
    public static boolean getStopTest() { return stopTest; }
    public static void setStopTest(boolean stop) { stopTest = stop; }
    public static void setActiveThreadGroup(ThreadGroup tg) { activeThreadGroup = tg; }
    public static void setActiveThreads(int num) { activeThreads = num;}
    public static JMeterVariables getjMeterVariables() { return jMeterVariables; }
    public static void setjMeterVariables(JMeterVariables vars) { jMeterVariables = vars; }
    public static Properties getjMeterProperties() { return jMeterProperties; }
    public static HashTree getTestPlanTree() { return testPlanTree; }
    public static HashSet<ThreadGroup> getTestThreadGroups() { return testThreadGroups; }
    public static void setTestThreadGroups(HashSet<ThreadGroup> threadGroupHashSet) { LiveChanges.testThreadGroups = threadGroupHashSet; }
    public static int getStaticCalcRate() { return staticCalcRate; }
    public static void setStaticCalcRate(int staticCalcRate) { LiveChanges.staticCalcRate = staticCalcRate; }

}
