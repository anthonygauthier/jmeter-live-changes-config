package io.github.delirius325.jmeter.config.livechanges;

import io.github.delirius325.jmeter.config.livechanges.api.App;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ThreadGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class LiveChanges extends ConfigTestElement implements TestBean, LoopIterationListener, TestStateListener {
    private static final Logger logger = LoggerFactory.getLogger(LiveChanges.class);

    private App app;
    private int httpServerPort;
    private static int activeThreads;
    private static JMeterVariables jMeterVariables;
    private static Properties jMeterProperties;

    @Override
    public void testStarted() {
        this.startServer();
    }

    @Override
    public void testStarted(String host) {
        this.startServer();
    }

    @Override
    public void testEnded() {
        this.app.stop();
    }

    @Override
    public void testEnded(String host) {
        this.app.stop();
    }

    @Override
    public void iterationStart(LoopIterationEvent event) {
        ThreadGroup threadGroup = (ThreadGroup) JMeterContextService.getContext().getThreadGroup();
        JMeterVariables vars = JMeterContextService.getContext().getVariables();
        Properties props = JMeterContextService.getContext().getProperties();
        String tmp = JMeterContextService.getContext().getThread().getThreadName();
        String threadName = tmp.substring(0, tmp.lastIndexOf("-"));

        this.checkForThreadChanges(threadGroup, event, threadName);
        this.checkForVariableChanges(vars, event);
        this.checkForPropertyChanges(props, event);
    }

    private void startServer() {
        this.app = new App(this.httpServerPort);
        this.app.start();
        logger.info(String.format("Exposed API on port %d", this.httpServerPort));
    }

    public void checkForThreadChanges(ThreadGroup threadGroup, LoopIterationEvent event, String threadName) {
        if(activeThreads != threadGroup.numberOfActiveThreads() && event.getIteration() > 1) {
            int diff = threadGroup.numberOfActiveThreads() - activeThreads;

            if(diff != 0) {
                for(int i=0; i < Math.abs(diff); i++) {
                    if(diff < 0) {
                        threadGroup.addNewThread(0, new StandardJMeterEngine());
                    } else {
                        logger.warn("Stopping: " + threadName + "-" + threadGroup.getNumThreads());
                        threadGroup.stopThread(threadName + "-" + threadGroup.numberOfActiveThreads(), true);
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

    public void checkForVariableChanges(JMeterVariables vars, LoopIterationEvent event) {
        jMeterVariables = vars;
        JMeterContextService.getContext().setVariables(jMeterVariables);
    }
    public void checkForPropertyChanges(Properties props, LoopIterationEvent event) {
        jMeterProperties = props;
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
    public static void setActiveThreads(int num) {
        activeThreads = num;
    }
    public static JMeterVariables getjMeterVariables() { return jMeterVariables; }
    public static void setjMeterVariables(JMeterVariables vars) { jMeterVariables = vars; }
    public static Properties getjMeterProperties() { return jMeterProperties; }
    public static void setjMeterProperties(Properties props) { jMeterProperties = props; }
}
