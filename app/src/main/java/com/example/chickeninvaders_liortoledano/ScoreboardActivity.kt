package com.example.chickeninvaders_liortoledano

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

class ScoreboardActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var scoresFragment: ScoresFragment
    private lateinit var mapFragment: MapsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scoreboard)

        findViews()
        initViews()
        setupFragments()
    }

    private fun findViews() {
        tabLayout = findViewById(R.id.scoreboard_TAB_layout)
    }

    private fun initViews() {
        tabLayout.addTab(tabLayout.newTab().setText("High Scores"))
        tabLayout.addTab(tabLayout.newTab().setText("Map"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showFragment(scoresFragment)
                    1 -> showFragment(mapFragment)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupFragments() {
        scoresFragment = ScoresFragment()
        mapFragment = MapsFragment()

        // Show scores fragment by default
        showFragment(scoresFragment)
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.scoreboard_FRAME_container, fragment)
            .commit()
    }

    fun onScoreClicked(score: Score) {
        // Switch to map tab and show location
        tabLayout.selectTab(tabLayout.getTabAt(1))
        mapFragment.showScoreLocation(score)
    }
}