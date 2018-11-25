package com.vitec.task.smartrule.utils;

import com.vitec.task.smartrule.bean.OptionMeasure;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OptionsMeasureUtils {

    /**
     *"measure":[
     {
     "id":1,
     "operate":1,
     "standard":8,
     "data":"<=6mm"},
     {
     "id":2,
     "operate":1,
     "standard":10,
     "data":">=6mm"
     }
     ]
     * @param measure
     * @return
     */
    public static List<OptionMeasure> getOptionMeasure(String measure) {
        List<OptionMeasure> measureList = new ArrayList<>();
        if (measure.length() > 2) {
            try {
                JSONArray measureJsonArray = new JSONArray(measure);
                for (int i=0;i<measureJsonArray.length();i++) {
                    JSONObject measureJson = measureJsonArray.getJSONObject(i);
                    OptionMeasure optionMeasure = new OptionMeasure();
                    optionMeasure.setId(measureJson.optInt("id"));
                    optionMeasure.setOperate(measureJson.optInt("operate"));
                    optionMeasure.setStandard(measureJson.optInt("standard"));
                    optionMeasure.setData(measureJson.optString("data"));
                    measureList.add(optionMeasure);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return measureList;
    }
}
