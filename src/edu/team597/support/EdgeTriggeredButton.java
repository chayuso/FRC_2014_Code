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
public class EdgeTriggeredButton {
    
    private boolean currentState;
    private boolean previousState;
    
    public EdgeTriggeredButton(boolean startState) {
        currentState = startState;
        previousState = startState;      
        
    }
    
    public void setCurrentState(boolean state) {
        previousState = currentState;
        currentState = state;
        
    }
    
    public boolean getCurrentState() {
        return currentState;
    }
    
    public boolean getRisingEdge() {
        return (currentState && !previousState);
    }
    
    public boolean getFallingEdge() {
        return (!currentState && previousState);
    }
    }
    

