package io.github.delirius325.jmeter.config.livechanges;

import org.apache.jmeter.samplers.SampleResult;

import java.util.HashMap;

public class SamplerMap {
    private HashMap<String, ResultHolder> map;
    private HashMap<String, SampleResult> rawMap;

    public SamplerMap(){
        this.map = new HashMap<>();
        this.rawMap = new HashMap<>();
    }

    public void add(SampleResult sr) {
        ResultHolder resultHolder = this.map.get(sr.getSampleLabel());

        if(resultHolder == null) {
            resultHolder = new ResultHolder(sr.getSampleLabel());
        }

        resultHolder.calculate(sr);
        this.map.put(sr.getSampleLabel(), resultHolder);
        this.rawMap.put(sr.getSampleLabel(), sr);
    }

    public HashMap<String, ResultHolder> getMap() {
        return map;
    }
    public HashMap<String, SampleResult> getRawMap() {
        return rawMap;
    }
}
