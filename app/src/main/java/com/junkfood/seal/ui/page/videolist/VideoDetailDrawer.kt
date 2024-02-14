@file:OptIn(ExperimentalMaterialApi::class)

package com.junkfood.seal.ui.page.videolist

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.ui.component.FilledTonalButtonWithIcon
import com.junkfood.seal.ui.component.LongTapTextButton
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.SealModalBottomSheetM2
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.ToastUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailDrawer(
    sheetState: ModalBottomSheetState,
    info: DownloadedVideoInfo,
    onDismissRequest: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    BackHandler(sheetState.targetValue == ModalBottomSheetValue.Expanded) {
        onDismissRequest()
    }

    val shareTitle = stringResource(id = R.string.share)
    with(info) {
        VideoDetailDrawerImpl(
            sheetState = sheetState,
            title = videoTitle,
            author = videoAuthor,
            url = videoUrl,
            onDismissRequest = onDismissRequest,
            onDelete = {
                onDismissRequest()
                onDelete()
            }, onOpenLink = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismissRequest()
                uriHandler.openUri(videoUrl)
            }, onShareFile = {
                FileUtil.createIntentForSharingFile(videoPath)?.runCatching {
                    context.startActivity(
                        Intent.createChooser(this, shareTitle)
                    )
                }
            })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun DrawerPreview() {
    VideoDetailDrawerImpl(
        sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailDrawerImpl(
    sheetState: ModalBottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden),
    title: String = stringResource(id = R.string.video_title_sample_text),
    author: String = stringResource(id = R.string.video_creator_sample_text),
    url: String = "https://www.example.com",
    onDismissRequest: () -> Unit = {},
    onDelete: () -> Unit = {},
    onOpenLink: () -> Unit = {},
    onShareFile: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    SealModalBottomSheetM2(drawerState = sheetState,
        horizontalPadding = PaddingValues(horizontal = 20.dp),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (author != "playlist" && author != "null")
                    SelectionContainer {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            text = author,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
            }
            Row(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .fillMaxWidth()
            ) {
                LongTapTextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(url))
                        ToastUtil.makeToast(context.getString(R.string.link_copied))
                    },
                    onClickLabel = stringResource(id = R.string.copy_link),
                    onLongClick = onOpenLink,
                    onLongClickLabel = stringResource(R.string.open_url)
                ) {
                    Icon(Icons.Outlined.Link, stringResource(R.string.video_url))
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        text = url, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 24.dp)
                    .padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End
            ) {

                OutlinedButtonWithIcon(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    onClick = onDelete,
                    icon = Icons.Outlined.Delete,
                    text = stringResource(R.string.remove)
                )

                FilledTonalButtonWithIcon(
                    onClick = onShareFile,
                    icon = Icons.Outlined.Share,
                    text = stringResource(R.string.share)
                )
            }
        })
}
