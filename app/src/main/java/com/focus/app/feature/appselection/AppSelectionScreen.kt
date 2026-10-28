package com.focus.app.feature.appselection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focus.app.R
import com.focus.app.core.designsystem.FocusAccent
import com.focus.app.core.designsystem.FocusAccentSubtle
import com.focus.app.core.designsystem.FocusBackground
import com.focus.app.core.designsystem.FocusBorder
import com.focus.app.core.designsystem.FocusCard
import com.focus.app.core.designsystem.FocusOnSurface
import com.focus.app.core.designsystem.FocusOnSurfaceDim
import com.focus.app.core.designsystem.FocusOnSurfaceVariant
import com.focus.app.core.designsystem.components.FocusAppListItem
import com.focus.app.core.designsystem.components.FocusEmptyState
import com.focus.app.core.designsystem.components.FocusPrimaryButton
import com.focus.app.core.designsystem.components.FocusSecondaryButton
import com.focus.app.core.designsystem.components.FocusTextButton

/**
 * Pantalla de selección de aplicaciones instaladas a restringir durante sesiones de enfoque.
 *
 * Ofrece búsqueda en tiempo real con debounce, conteo de apps seleccionadas,
 * persistencia inmediata en DataStore y atajos para deseleccionar todo.
 *
 * @param onNavigateBack Acción al presionar el botón de retroceso o completar la selección.
 * @param viewModel ViewModel inyectado por Hilt.
 */
@Composable
fun AppSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FocusBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Barra superior ───────────────────────────────────────────────
            AppSelectionTopBar(
                selectedCount = uiState.selectedCount,
                onNavigateBack = onNavigateBack,
                onClearAll = { viewModel.onClearAll() }
            )

            // ── Barra de búsqueda ────────────────────────────────────────────
            AppSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onClearQuery = {
                    viewModel.onSearchQueryChanged("")
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // ── Contenido de la lista / Estados ──────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = FocusAccent,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.app_selection_loading),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FocusOnSurfaceVariant
                                )
                            }
                        }
                    }

                    uiState.errorMessage != null -> {
                        FocusEmptyState(
                            icon = Icons.Rounded.Warning,
                            title = stringResource(R.string.app_selection_error),
                            description = uiState.errorMessage,
                            action = {
                                FocusSecondaryButton(
                                    text = stringResource(R.string.error_retry),
                                    onClick = { viewModel.onRetry() },
                                    modifier = Modifier.width(180.dp)
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    uiState.isEmpty -> {
                        FocusEmptyState(
                            icon = Icons.Rounded.SearchOff,
                            title = stringResource(R.string.app_selection_empty),
                            description = if (uiState.isSearching) {
                                "No apps match \"${uiState.searchQuery}\""
                            } else {
                                "No launchable third-party apps found"
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 8.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.filteredApps,
                                key = { it.packageName }
                            ) { app ->
                                val isSelected = app.packageName in uiState.selectedPackageNames
                                FocusAppListItem(
                                    appName = app.displayName,
                                    packageName = app.packageName,
                                    icon = app.icon,
                                    isSelected = isSelected,
                                    onClick = { viewModel.onToggleAppSelection(app.packageName) }
                                )
                            }
                        }
                    }
                }
            }

            // ── Barra inferior fija con botón de confirmación ────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = FocusBackground.copy(alpha = 0.95f),
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    FocusPrimaryButton(
                        text = if (uiState.selectedCount > 0) {
                            "${stringResource(R.string.app_selection_done)} (${uiState.selectedCount})"
                        } else {
                            stringResource(R.string.app_selection_done)
                        },
                        onClick = onNavigateBack
                    )
                }
            }
        }
    }
}

/**
 * Encabezado de la pantalla de selección con botón de retroceso y acciones rápidas.
 */
@Composable
private fun AppSelectionTopBar(
    selectedCount: Int,
    onNavigateBack: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = FocusOnSurface
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_selection_title),
                style = MaterialTheme.typography.titleMedium,
                color = FocusOnSurface
            )
            Text(
                text = stringResource(R.string.app_selection_selected_count, selectedCount),
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedCount > 0) FocusAccent else FocusOnSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = selectedCount > 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FocusTextButton(
                text = stringResource(R.string.app_selection_clear_all),
                onClick = onClearAll,
                contentColor = FocusOnSurfaceVariant
            )
        }
    }
}

/**
 * Campo de texto estilizado para búsqueda de aplicaciones con ícono y botón de borrado.
 */
@Composable
private fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = stringResource(R.string.app_selection_search_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = FocusOnSurfaceDim
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = FocusOnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = "Clear search",
                        tint = FocusOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FocusCard,
            unfocusedContainerColor = FocusCard,
            focusedBorderColor = FocusAccent,
            unfocusedBorderColor = FocusBorder,
            focusedTextColor = FocusOnSurface,
            unfocusedTextColor = FocusOnSurface,
            cursorColor = FocusAccent
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}
