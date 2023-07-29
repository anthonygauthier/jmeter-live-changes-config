package io.github.delirius325.jmeter.config.livechanges;


import org.apache.jmeter.samplers.SampleResult;

import java.util.HashMap;

public class SamplerMap {
    private HashMap<String, ResultHolder> map;

    public SamplerMap(){
        map = new HashMap<>();
    }

    public void add(SampleResult sr) {
        ResultHolder resultHolder = this.map.get(sr.getSampleLabel());

        if(resultHolder == null) {
            resultHolder = new ResultHolder();
        }

        resultHolder.calculate(sr);
        this.map.put(sr.getSampleLabel(), resultHolder);
    }

    public HashMap<String, ResultHolder> getMap() {
        return this.map;
    }
}
