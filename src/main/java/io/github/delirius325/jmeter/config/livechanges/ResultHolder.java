package io.github.delirius325.jmeter.config.livechanges;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.Calculator;

import java.util.*;

public class ResultHolder {
    private Calculator calculator;
    private ArrayList<Long> responseTimeList;
    private long ninetiethPercentile;
    private long totalLatency;
    private long avgLatency;
    private int totalErrors;

    public ResultHolder(String label){
        this.responseTimeList = new ArrayList<>();
        this.totalErrors = 0;
        this.totalLatency = 0;
        this.avgLatency = 0;
        this.calculator = new Calculator(label);
    }

    private long calculate90thPercentile() {
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
        this.responseTimeList.add(sr.getTime());
        this.calculator.addSample(sr);
        this.ninetiethPercentile = this.calculate90thPercentile();
        this.totalLatency += sr.getLatency();
        this.avgLatency = (this.totalLatency / this.calculator.getCount());
        this.totalErrors = (!sr.isSuccessful()) ? this.totalErrors++ : this.totalErrors;
    }

    public Calculator getCalculator() {
        return this.calculator;
    }
    public double getAvgLatency() { return this. avgLatency; }
    public double getNinetiethPercentile() { return this.ninetiethPercentile; }
    public int getTotalErrors() { return this.totalErrors; }
}
