/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.team597.support;

/**
 *
 * @author Team597
 */
public class ToggleButton {
    private boolean currentState;
    private EdgeTriggeredButton edgeTriggeredButton;
    
    public ToggleButton(boolean initialState){
        currentState = initialState;
        edgeTriggeredButton = new EdgeTriggeredButton(false);
    }
    
    public boolean getCurrentState() {
        return currentState;
    }
    public void setCurrentState(boolean inputState){
        edgeTriggeredButton.setCurrentState(inputState);
        
        // If we are pressing the button...
        if(edgeTriggeredButton.getRisingEdge() == true){
        currentState = !currentState;
        }
    }
    
    public void setInternalToggleState(boolean inputState) {
        currentState = inputState;
    }
}
