package io.github.delirius325.jmeter.config.livechanges;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.ojalgo.random.SampleSet;

import java.util.*;

public class ResultHolder {
    private HashSet<Long> responseTimeSet;
    private long avgLatency;
    private long avgResponseTime;
    private long ninetyPercentile;
    private long minResponseTime;
    private long maxResponseTime;
    private long stdDeviation;
    private long errorPercentage;
    private long hitsPerSecond;
    private long receivedBytesPerSec;
    private long sentBytesPerSec;
    private long avgBytes;
    private long totalBytes;
    private long totalSentBytes;
    private int totalErrors;
    private int totalSamples;

    public ResultHolder(){
        this.responseTimeSet = new HashSet<>();
        this.avgLatency = 0;
        this.avgResponseTime = 0;
        this.ninetyPercentile = 0;
        this.minResponseTime = 0;
        this.maxResponseTime = 0;
        this.stdDeviation = 0;
        this.errorPercentage = 0;
        this.hitsPerSecond = 0;
        this.receivedBytesPerSec = 0;
        this.sentBytesPerSec = 0;
        this.avgBytes = 0;
        this.totalErrors = 0;
        this.totalSamples = 0;
    }

    private long getStandardDeviation() {
        long stdDeviation = 0;
        long totalSquaredDiff = 0;
        HashSet<Long> squaredDiff = new HashSet<>();

        // subtract, square and add to a temporary set
        this.responseTimeSet.forEach(number -> {
            number = (number - this.avgResponseTime)^2;
            squaredDiff.add(number);
        });

        // calculate the total of the temp set
        Iterator<Long> it = squaredDiff.iterator();
        while(it.hasNext()) {
            totalSquaredDiff += it.next();
        }

        // calculate standard deviation
        stdDeviation = (totalSquaredDiff / squaredDiff.size())^2;

        return stdDeviation;
    }

    private long get90thPercentile() {
        List<Long> responseTimeList = new ArrayList<>(responseTimeSet);
        float index = Math.round(0.9 * responseTimeList.size());
        long percentile = 0;

        // sort list from low to high
        Collections.sort(responseTimeList);

        for(int i=0; i < responseTimeList.size(); i++) {
            if(i == index-1) {
                percentile = responseTimeList.get(i);
            }
        }

        return percentile;
    }

    public void calculate(SampleResult sr) {
        this.responseTimeSet.add(sr.getTime());
        this.totalSamples++;
        this.totalErrors = (!sr.isSuccessful()) ? this.totalErrors + 1 : this.totalErrors;
        this.avgLatency += (sr.getLatency() / this.totalSamples);
        this.avgResponseTime += (sr.getTime() / this.totalSamples);
        this.minResponseTime = (this.minResponseTime > sr.getTime()) ? sr.getTime() : this.minResponseTime;
        this.maxResponseTime = (this.maxResponseTime < sr.getTime()) ? sr.getTime() : this.maxResponseTime;
        this.errorPercentage = (this.totalErrors * 100) / this.totalSamples;
        this.hitsPerSecond = (this.totalSamples * 1000) / (System.currentTimeMillis() - JMeterContextService.getTestStartTime());
        this.totalBytes += sr.getBytesAsLong();
        this.totalSentBytes += sr.getSentBytes();
        this.avgBytes = (this.totalBytes / this.totalSamples);
        this.sentBytesPerSec = (this.totalSentBytes * 1000) / (System.currentTimeMillis() - JMeterContextService.getTestStartTime());
        this.receivedBytesPerSec = (this.totalBytes * 1000) / (System.currentTimeMillis() - JMeterContextService.getTestStartTime());

        this.ninetyPercentile = this.get90thPercentile();
        this.stdDeviation = this.getStandardDeviation();
    }

    public double getAvgLatency() {
        return avgLatency;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public double getNinetyPercentile() {
        return ninetyPercentile;
    }

    public double getMinResponseTime() {
        return minResponseTime;
    }

    public double getMaxResponseTime() {
        return maxResponseTime;
    }

    public double getStdDeviation() {
        return stdDeviation;
    }

    public double getErrorPercentage() {
        return errorPercentage;
    }

    public double getHitsPerSecond() {
        return hitsPerSecond;
    }

    public double getReceivedBytesPerSec() {
        return receivedBytesPerSec;
    }

    public double getSentBytesPerSec() {
        return sentBytesPerSec;
    }

    public long getAvgBytes() {
        return avgBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getTotalSentBytes() {
        return totalSentBytes;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public int getTotalSamples() {
        return totalSamples;
    }
}
