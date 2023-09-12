package com.ramitsuri.choresclient.android.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.view.TextValue
import com.ramitsuri.choresclient.model.filter.HouseFilter
import com.ramitsuri.choresclient.model.filter.HouseFilterItem
import com.ramitsuri.choresclient.model.filter.PersonFilter
import com.ramitsuri.choresclient.model.filter.PersonFilterItem

class FilterPreview : PreviewParameterProvider<List<Filter>> {
    override val values: Sequence<List<Filter>>
        get() = sequenceOf(
            listOf(
                PersonFilter(
                    text = TextValue.ForString("Jess"),
                    items = listOf(
                        PersonFilterItem(
                            id = "1",
                            displayName = TextValue.ForString("Ramit"),
                            selected = false
                        ),
                        PersonFilterItem(
                            id = "2",
                            displayName = TextValue.ForString("Jess"),
                            selected = true
                        ),
                        PersonFilterItem(
                            id = "3",
                            displayName = TextValue.ForString("All"),
                            selected = false
                        )
                    )
                ),
                HouseFilter(
                    text = TextValue.ForString("House+1"),
                    items = listOf(
                        HouseFilterItem(
                            id = "1",
                            displayName = TextValue.ForString("House"),
                            selected = true
                        ),
                        HouseFilterItem(
                            id = "2",
                            displayName = TextValue.ForString("Personal"),
                            selected = true
                        ),
                        HouseFilterItem(
                            id = "3",
                            displayName = TextValue.ForString("All"),
                            selected = false
                        )
                    )
                )
            )
        )
}