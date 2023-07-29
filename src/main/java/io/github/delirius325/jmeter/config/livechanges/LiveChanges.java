package io.github.delirius325.jmeter.config.livechanges;

import io.github.delirius325.jmeter.config.livechanges.api.App;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.ThreadGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveChanges extends ConfigTestElement implements TestBean, LoopIterationListener, TestStateListener {
    private static final Logger logger = LoggerFactory.getLogger(LiveChanges.class);

    private App app;
    private int httpServerPort;
    private static int activeThreads;

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

    // On loop iteration
    @Override
    public void iterationStart(LoopIterationEvent event) {
        ThreadGroup threadGroup = (ThreadGroup) JMeterContextService.getContext().getThreadGroup();
        checkForThreadChange(threadGroup, event);
    }


    public synchronized int getHttpServerPort() {
        return this.httpServerPort;
    }

    public synchronized void setHttpServerPort(int port) {
        this.httpServerPort = port;
    }

    private void startServer() {
        this.app = new App(this.httpServerPort);
        this.app.start();
        logger.info(String.format("Exposed API on port %d", this.httpServerPort));
    }

    public static int getActiveThreads() {
        return activeThreads;
    }

    public static void setActiveThreads(int num) {
        activeThreads = num;
    }

    public void checkForThreadChange(ThreadGroup threadGroup, LoopIterationEvent event) {
        if(activeThreads != JMeterContextService.getNumberOfThreads() && event.getIteration() > 1) {
            int diff = threadGroup.numberOfActiveThreads() - activeThreads;

            // if diff is negative, then add threads
            if(diff != 0) {
                for(int i=0; i < Math.abs(diff); i++) {
                    if(diff < 0) {
                        threadGroup.addNewThread(0, new StandardJMeterEngine());
                    } else {
                        // figure out a way to stop specific threads
                    }
                }
            } else {
                threadGroup.tellThreadsToStop();
            }
        } else {
            activeThreads = threadGroup.numberOfActiveThreads();
        }
    }
}
