package com.vitec.task.smartrule.bean;

/**
 * 服务器下发Json数据中，其中有一个是measure json数组
 * 是测量数据的标准，数据模板如下：
 *  "measure":[
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
 */
public class OptionMeasure {

    private int id;//id
    private int operate;//操作标志，1-小于等于
    private int standard;//标准数据，例如，操作标志是小于等于，则测量数据就要小于等于 这个standard的数据才算合格
    private String data;//层高选择，是给用户选择的，用户

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOperate() {
        return operate;
    }

    public void setOperate(int operate) {
        this.operate = operate;
    }

    public int getStandard() {
        return standard;
    }

    public void setStandard(int standard) {
        this.standard = standard;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "OptionMeasure{" +
                "id=" + id +
                ", operate=" + operate +
                ", standard=" + standard +
                ", data='" + data + '\'' +
                '}';
    }
}
