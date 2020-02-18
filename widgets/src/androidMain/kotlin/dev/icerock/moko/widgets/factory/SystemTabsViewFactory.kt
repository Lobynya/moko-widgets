/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.widgets.factory

import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TabHost
import android.widget.TabWidget
import android.widget.TextView
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.widgets.TabsWidget
import dev.icerock.moko.widgets.core.ViewBundle
import dev.icerock.moko.widgets.core.ViewFactory
import dev.icerock.moko.widgets.core.ViewFactoryContext
import dev.icerock.moko.widgets.style.applyBackgroundIfNeeded
import dev.icerock.moko.widgets.style.applyPaddingIfNeeded
import dev.icerock.moko.widgets.style.background.Background
import dev.icerock.moko.widgets.style.view.MarginValues
import dev.icerock.moko.widgets.style.view.PaddingValues
import dev.icerock.moko.widgets.style.view.WidgetSize
import dev.icerock.moko.widgets.utils.bindNotNull

actual class SystemTabsViewFactory actual constructor(
    private val background: Background?,
    private val padding: PaddingValues?,
    private val margins: MarginValues?
) : ViewFactory<TabsWidget<out WidgetSize>> {

    override fun <WS : WidgetSize> build(
        widget: TabsWidget<out WidgetSize>,
        size: WS,
        viewFactoryContext: ViewFactoryContext
    ): ViewBundle<WS> {
        val context = viewFactoryContext.context
        val lifecycleOwner = viewFactoryContext.lifecycleOwner

        val tabHost = TabHost(context).apply {
            id = android.R.id.tabhost

            applyBackgroundIfNeeded(this@SystemTabsViewFactory.background)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        tabHost.addView(
            container,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val tabWidget = TabWidget(context).apply {
            id = android.R.id.tabs
        }

        container.addView(
            tabWidget,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val content = FrameLayout(context).apply {
            id = android.R.id.tabcontent

            applyPaddingIfNeeded(padding)
        }
        container.addView(
            content,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        tabHost.setup()

        widget.tabs.forEachIndexed { index, tab ->
            fun createIndicator(stringDesc: StringDesc): View {
                val tabContainer = FrameLayout(
                    ContextThemeWrapper(
                        context,
                        android.R.style.Widget_DeviceDefault_Tab
                    )
                )
                val text = TextView(
                    ContextThemeWrapper(
                        context,
                        android.R.style.Widget_DeviceDefault_ActionBar_TabText
                    )
                ).apply {
                    text = stringDesc.toString(context)
                }
                tabContainer.addView(
                    text, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                    )
                )
                return tabContainer
            }

            val tabSpec = tabHost.newTabSpec("tab$index").apply {
                setContent {
                    tab.body.buildView(
                        ViewFactoryContext(
                            context = context,
                            parent = content,
                            lifecycleOwner = lifecycleOwner
                        )
                    ).view // TODO apply margins?
                }
                setIndicator(createIndicator(tab.title.value))
            }
            tab.title.bindNotNull(lifecycleOwner) { tabSpec.setIndicator(createIndicator(it)) }
            tabHost.addTab(tabSpec)
        }

        return ViewBundle(
            view = tabHost,
            size = size,
            margins = margins
        )
    }
}
