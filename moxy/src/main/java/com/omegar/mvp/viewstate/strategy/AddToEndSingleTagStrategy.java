package com.omegar.mvp.viewstate.strategy;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.ViewCommand;

import java.util.Iterator;
import java.util.List;

/**
 * Command will be added to the end of the commands queue. If the commands queue contains the same tag, then
 * an existing command will be removed.
 */
class AddToEndSingleTagStrategy implements StateStrategy {

    @Override
    public <View extends MvpView> void beforeApply(List<ViewCommand<View>> currentState, ViewCommand<View> incomingCommand) {
        Iterator<ViewCommand<View>> iterator = currentState.iterator();

        while (iterator.hasNext()) {
            ViewCommand<View> entry = iterator.next();

            if (entry.getTag().equals(incomingCommand.getTag())) {
                iterator.remove();
            }
        }

        currentState.add(incomingCommand);
    }

    @Override
    public <View extends MvpView> void afterApply(List<ViewCommand<View>> currentState, ViewCommand<View> incomingCommand) {
        //Just do nothing
    }

}
