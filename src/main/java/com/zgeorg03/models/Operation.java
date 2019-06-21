package com.zgeorg03.models;

public abstract class Operation {
    protected  String id;
    protected  int weight;

    @Override
    public String toString() {
        return "Operation{" +
                "id='" + id + '\'' +
                ", weight=" + weight +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }


    public int getWeight() {
        return weight;
    }

    public Operation(){

    }
    public Operation(String operationId, int weight) {
        this.id = operationId;
        this.weight = weight;
    }

}
