/*
 * Copyright (c) 2022, Ben Jilks <benjyjilks@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example.burnercontroller

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView

class DatabaseUpdateProgress : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_database_update_progress, container, false)
    }

    fun updateHandler(looper: Looper): Handler {
        return ProgressUpdateHandler(looper)
    }

    private inner class ProgressUpdateHandler(looper: Looper): Handler(looper) {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)

            val progressBar = requireView().findViewById<ProgressBar>(R.id.progress_bar)
            val percentDisplay = requireView().findViewById<TextView>(R.id.percent_display)

            val progress = message.data.getDouble("progress")
            progressBar.progress = progress.toInt()
            percentDisplay.text = "%.1f%%".format(progress)
        }
    }

}
