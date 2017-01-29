package com.funk.paupowpow.ducky.viewholder;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.funk.paupowpow.ducky.controller.QuestOverviewItemViewController;
import com.funk.paupowpow.ducky.model.data.DuckyDatabaseHandler;
import com.funk.paupowpow.ducky.model.data.Quest;

import java.io.File;
import java.io.IOException;

/**
 * Created by paulahaertel on 10.12.16.
 */

public class QuestOverviewItemViewHolder extends RecyclerView.ViewHolder {

    private QuestOverviewItemViewController viewController;

    public QuestOverviewItemViewHolder(View itemView, Activity activity) {
        super(itemView);
        viewController = new QuestOverviewItemViewController(itemView, activity);
    }

    public QuestOverviewItemViewController getQuestOverviewItemViewController() {
        return viewController;
    }

    public void bind(Quest quest) {
        viewController.bindData(quest);
    }

    public void onViewRecycled() {
        viewController.onViewRecycled();
    }

    public void onViewAttachedToWindow() {
        viewController.onViewAttachedToWindow();
    }

    public void onViewDetachedFromWindow() {
        viewController.onViewDetachedFromWindow();
    }



}
