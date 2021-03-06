package io.github.droidkaigi.confsched2020.session.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.ViewHolder
import dagger.Module
import dagger.Provides
import dagger.android.support.DaggerFragment
import io.github.droidkaigi.confsched2020.di.PageScope
import io.github.droidkaigi.confsched2020.ext.assistedActivityViewModels
import io.github.droidkaigi.confsched2020.ext.assistedViewModels
import io.github.droidkaigi.confsched2020.session.R
import io.github.droidkaigi.confsched2020.session.databinding.FragmentSearchSessionsBinding
import io.github.droidkaigi.confsched2020.session.ui.item.SectionHeaderItem
import io.github.droidkaigi.confsched2020.session.ui.item.SessionItem
import io.github.droidkaigi.confsched2020.session.ui.item.SpeakerItem
import io.github.droidkaigi.confsched2020.session.ui.viewmodel.SearchSessionsViewModel
import io.github.droidkaigi.confsched2020.session.ui.viewmodel.SessionsViewModel
import io.github.droidkaigi.confsched2020.system.ui.viewmodel.SystemViewModel
import javax.inject.Inject
import javax.inject.Provider

class SearchSessionsFragment : DaggerFragment() {

    private lateinit var binding: FragmentSearchSessionsBinding

    @Inject lateinit var searchSessionsModelFactory: SearchSessionsViewModel.Factory
    private val searchSessionsViewModel by assistedViewModels {
        searchSessionsModelFactory.create()
    }

    @Inject lateinit var sessionsViewModelProvider: Provider<SessionsViewModel>
    private val sessionsViewModel: SessionsViewModel by assistedActivityViewModels {
        sessionsViewModelProvider.get()
    }

    @Inject lateinit var systemViewModelProvider: Provider<SystemViewModel>
    private val systemViewModel: SystemViewModel by assistedActivityViewModels {
        systemViewModelProvider.get()
    }

    @Inject
    lateinit var sessionItemFactory: SessionItem.Factory

    @Inject
    lateinit var speakerItemFactory: SpeakerItem.Factory

    @Inject
    lateinit var sectionHeaderItemFactory: SectionHeaderItem.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_search_sessions,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val groupAdapter = GroupAdapter<ViewHolder<*>>()
        binding.searchSessionRecycler.adapter = groupAdapter

        searchSessionsViewModel.uiModel.observe(viewLifecycleOwner) { uiModel: SearchSessionsViewModel.UiModel ->
            groupAdapter.clear()

            if (uiModel.searchResult.speakers.isNotEmpty()) {
                groupAdapter.add(sectionHeaderItemFactory.create(resources.getString(R.string.speaker)))
                groupAdapter.addAll(uiModel.searchResult.speakers.map {
                    speakerItemFactory.create(it)
                })
            }
            
            if (uiModel.searchResult.sessions.isNotEmpty()) {
                groupAdapter.add(sectionHeaderItemFactory.create(resources.getString(R.string.session)))
                groupAdapter.addAll(uiModel.searchResult.sessions.map {
                    sessionItemFactory.create(it, sessionsViewModel)
                })
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search_sessions, menu)
        val searchView = menu.findItem(R.id.search_view).actionView as SearchView
        searchView.isIconified = false
        searchView.queryHint = resources.getString(R.string.query_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                searchSessionsViewModel.updateSearchQuery(s)
                return false
            }
        })
    }

    companion object {
        fun newInstance(): SearchSessionsFragment {
            return SearchSessionsFragment()
        }
    }
}

@Module
abstract class SearchSessionsFragmentModule {
    @Module
    companion object {
        @PageScope
        @JvmStatic @Provides fun providesLifeCycleLiveData(
            searchSessionsFragment: SearchSessionsFragment
        ): LiveData<LifecycleOwner> {
            return searchSessionsFragment.viewLifecycleOwnerLiveData
        }
    }
}
