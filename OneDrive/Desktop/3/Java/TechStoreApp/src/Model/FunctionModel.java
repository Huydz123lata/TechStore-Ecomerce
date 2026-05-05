/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author HUY0406
 */
public class FunctionModel {

    private int functionId;
    private String nameFunction;

    public FunctionModel() {
    }

    public FunctionModel(int functionId, String nameFunction) {
        this.functionId = functionId;
        this.nameFunction = nameFunction;
    }

    // Getter và Setter
    public int getFunctionId() {
        return functionId;
    }

    public void setFunctionId(int functionId) {
        this.functionId = functionId;
    }

    public String getNameFunction() {
        return nameFunction;
    }

    public void setNameFunction(String nameFunction) {
        this.nameFunction = nameFunction;
    }

    @Override
    public String toString() {
        return this.nameFunction;
    }
}
