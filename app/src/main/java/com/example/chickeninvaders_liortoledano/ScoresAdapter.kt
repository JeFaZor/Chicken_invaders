package com.example.chickeninvaders_liortoledano

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScoresAdapter(private val onScoreClick: (Score) -> Unit) :
    RecyclerView.Adapter<ScoresAdapter.ScoreViewHolder>() {

    private var scores = listOf<Score>()

    fun updateScores(newScores: List<Score>) {
        scores = newScores
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        holder.bind(scores[position], position + 1)
    }

    override fun getItemCount() = scores.size

    inner class ScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankText: TextView = itemView.findViewById(R.id.score_LBL_rank)
        private val nameText: TextView = itemView.findViewById(R.id.score_LBL_name)
        private val scoreText: TextView = itemView.findViewById(R.id.score_LBL_score)
        private val dateText: TextView = itemView.findViewById(R.id.score_LBL_date)

        fun bind(score: Score, rank: Int) {
            rankText.text = "#$rank"
            nameText.text = score.playerName
            scoreText.text = score.score.toString()
            dateText.text = score.date

            itemView.setOnClickListener {
                onScoreClick(score)
            }
        }
    }
}