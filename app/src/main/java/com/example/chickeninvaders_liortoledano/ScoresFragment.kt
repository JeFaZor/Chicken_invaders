package com.example.chickeninvaders_liortoledano

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chickeninvaders_liortoledano.utilities.ScoreManager

class ScoresFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var scoresAdapter: ScoresAdapter
    private lateinit var scoreManager: ScoreManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViews(view)
        initViews()
        loadScores()
    }

    private fun findViews(view: View) {
        recyclerView = view.findViewById(R.id.scores_RV_list)
    }

    private fun initViews() {
        scoreManager = ScoreManager(requireContext())

        scoresAdapter = ScoresAdapter { score ->
            (activity as? ScoreboardActivity)?.onScoreClicked(score)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = scoresAdapter
    }

    private fun loadScores() {
        val scores = scoreManager.getTopScores()
        scoresAdapter.updateScores(scores)
    }
}