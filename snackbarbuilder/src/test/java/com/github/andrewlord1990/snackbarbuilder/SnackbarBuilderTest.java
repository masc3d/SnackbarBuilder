/*
 * Copyright (C) 2016 Andrew Lord
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.github.andrewlord1990.snackbarbuilder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar.Callback;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.andrewlord1990.snackbarbuilder.callback.SnackbarActionDismissCallback;
import com.github.andrewlord1990.snackbarbuilder.callback.SnackbarCallback;
import com.github.andrewlord1990.snackbarbuilder.callback.SnackbarConsecutiveDismissCallback;
import com.github.andrewlord1990.snackbarbuilder.callback.SnackbarDismissCallback;
import com.github.andrewlord1990.snackbarbuilder.callback.SnackbarManualDismissCallback;
import com.github.andrewlord1990.snackbarbuilder.callback.SnackbarShowCallback;
import com.github.andrewlord1990.snackbarbuilder.callback.SnackbarSwipeDismissCallback;
import com.github.andrewlord1990.snackbarbuilder.callback.SnackbarTimeoutDismissCallback;
import com.github.andrewlord1990.snackbarbuilder.parent.SnackbarParentFinder;
import com.github.andrewlord1990.snackbarbuilder.robolectric.LibraryRobolectricTestRunner;

import org.assertj.android.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LibraryRobolectricTestRunner.class)
public class SnackbarBuilderTest {

  @Mock
  CoordinatorLayout parentView;

  @Mock
  Activity activity;

  @Mock
  Resources resources;

  @Mock
  Callback callback;

  @Mock
  SnackbarCallback snackbarCallback;

  @Mock
  Drawable drawable;

  @Mock
  Snackbar snackbar;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    when(parentView.getContext()).thenReturn(RuntimeEnvironment.application);
  }

  @Test
  public void givenView_whenCreated_thenParentViewSetCorrectly() {
    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.parentView).isEqualTo(parentView);
    assertThat(builder.context).isEqualTo(parentView.getContext());
  }

  @Test
  public void givenActivity_whenCreated_thenParentViewFoundUsingParentViewId() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.setTheme(R.style.TestSnackbarBuilder_CustomTheme);
    LinearLayout layout = new LinearLayout(activity);
    layout.setId(R.id.snackbarbuilder_icon);
    activity.setContentView(layout);

    SnackbarBuilder builder = new SnackbarBuilder(activity);

    assertThat(builder.parentViewId).isEqualTo(R.id.snackbarbuilder_icon);
    assertThat(builder.parentView).isEqualTo(layout);
    assertThat(builder.context).isEqualTo(activity);
    assertThat(builder.actionTextColor).isEqualTo(0xFF454545);
    assertThat(builder.messageTextColor).isEqualTo(0xFF987654);
  }

  @Test
  public void givenActivityAndParentFinder_whenCreated_thenParentViewFoundUsingTheParentFinder() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    @IdRes final int fallbackId = 100;
    SnackbarParentFinder parentFinder = new SnackbarParentFinder() {
      @Override
      public View findSnackbarParent(Activity activity) {
        View defaultParent = activity.findViewById(R.id.snackbarbuilder_icon);
        if (defaultParent != null) {
          return defaultParent;
        }
        return activity.findViewById(fallbackId);
      }
    };
    LinearLayout layout = new LinearLayout(activity);
    layout.setId(fallbackId);
    activity.setContentView(layout);

    SnackbarBuilder builder = new SnackbarBuilder(activity, parentFinder);

    assertThat(builder.parentView).isEqualTo(layout);
    assertThat(builder.context).isEqualTo(activity);
  }

  @Test
  public void whenCreated_thenActionTextColorFromCustomThemeAttribute() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_CustomTheme);

    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.actionTextColor).isEqualTo(0xFF454545);
  }

  @Test
  public void givenNoCustomThemeAttribute_whenCreated_thenActionTextColorNotSet() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_FallbackTheme);

    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.actionTextColor).isEqualTo(0);
  }

  @Test
  public void whenCreated_thenMessageTextColorFromCustomThemeAttribute() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_CustomTheme);

    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.messageTextColor).isEqualTo(0xFF987654);
  }

  @Test
  public void givenNoCustomThemeAttribute_whenCreated_thenMessageTextColorNotSet() {
    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.messageTextColor).isEqualTo(0);
  }

  @Test
  public void whenCreated_thenDurationFromCustomThemeAttribute() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_CustomTheme);

    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.duration).isEqualTo(Snackbar.LENGTH_INDEFINITE);
  }

  @Test
  public void givenNoCustomThemeAttribute_whenCreated_thenDurationLong() {
    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.duration).isEqualTo(Snackbar.LENGTH_LONG);
  }

  @Test
  public void whenCreated_thenBackgroundColorFromCustomThemeAttribute() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_CustomTheme);

    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.backgroundColor).isEqualTo(0xFF999999);
  }

  @Test
  public void givenNoCustomThemeAttribute_whenCreated_thenBackgroundColorNotSet() {
    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.backgroundColor).isEqualTo(0);
  }

  @Test
  public void whenCreated_thenIconMarginFromCustomThemeAttribute() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_CustomTheme);

    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.iconMargin).isEqualTo(16);
  }

  @Test
  public void givenNoCustomThemeAttribute_whenCreated_thenIconMarginFromDimension() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_FallbackTheme);

    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.iconMargin).isEqualTo(RuntimeEnvironment.application.getResources()
        .getDimensionPixelSize(R.dimen.snackbarbuilder_icon_margin_default));
  }

  @Test
  public void whenCreated_thenActionAllCapsFalseFromCustomThemeAttribute() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_CustomTheme);

    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.actionAllCaps).isFalse();
  }

  @Test
  public void givenNoCustomThemeAttribute_whenCreated_thenActionAllCapsTrue() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_FallbackTheme);

    SnackbarBuilder builder = new SnackbarBuilder(parentView);

    assertThat(builder.actionAllCaps).isTrue();
  }

  @Test
  public void whenMessageWithString_thenMessageSet() {
    SnackbarBuilder builder = createBuilder();

    builder.message("message");

    assertThat(builder.message).isEqualTo("message");
  }

  @Test
  public void givenSpan_whenMessage_thenMessageSet() {
    SnackbarBuilder builder = createBuilder();
    Spannable spannable = new SpannableString("testMessage");
    spannable.setSpan(new ForegroundColorSpan(Color.CYAN), 0, spannable.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    builder.message(spannable);

    assertThat(builder.message.toString()).isEqualTo("testMessage");
    SpannableString actual = SpannableString.valueOf(builder.message);
    ForegroundColorSpan[] spans = actual.getSpans(0, spannable.length(), ForegroundColorSpan.class);
    assertThat(spans).hasSize(1);
    assertThat(spans[0].getForegroundColor()).isEqualTo(Color.CYAN);
  }

  @Test
  public void whenMessageWithStringResource_thenMessageSet() {
    SnackbarBuilder builder = createBuilder();

    builder.message(R.string.snackbarbuilder_action_undo);

    assertThat(builder.message).isEqualTo("Undo");
  }

  @Test
  public void whenMessageTextColorRes_thenMessageTextColorSet() {
    SnackbarBuilder builder = createBuilder();

    builder.messageTextColorRes(R.color.snackbarbuilder_default_message);

    assertThat(builder.messageTextColor).isEqualTo(Color.WHITE);
  }

  @Test
  public void whenMessageTextColor_thenMessageTextColorSet() {
    SnackbarBuilder builder = createBuilder();

    builder.messageTextColor(0xFF333333);

    assertThat(builder.messageTextColor).isEqualTo(0xFF333333);
  }

  @Test
  public void whenAppendMessageWithString_thenMessageAdded() {
    SnackbarBuilder builder = createBuilder();
    String message = "aMessage";

    builder.appendMessage(message);

    assertThat(builder.appendMessages.toString()).isEqualTo(message);
  }

  @Test
  public void givenMessageAlreadyAppended_whenAppendMessageWithString_thenMessageAdded() {
    SnackbarBuilder builder = createBuilder();
    String message = "aMessage";
    String starting = "startingMessage";
    builder.appendMessage(starting);

    builder.appendMessage(message);

    assertThat(builder.appendMessages.toString()).isEqualTo(starting + message);
  }

  @Test
  public void whenAppendMessageWithStringResource_thenMessageAdded() {
    SnackbarBuilder builder = createBuilder();

    builder.appendMessage(R.string.snackbarbuilder_action_undo);

    assertThat(builder.appendMessages.toString()).isEqualTo("Undo");
  }

  @Test
  public void givenMessageAlreadyAppended_whenAppendMessageWithStringResource_thenMessageAdded() {
    SnackbarBuilder builder = createBuilder();
    String starting = "startingMessage";
    builder.appendMessage(starting);

    builder.appendMessage(R.string.snackbarbuilder_action_undo);

    assertThat(builder.appendMessages.toString()).isEqualTo("startingMessageUndo");
  }

  @Test
  public void whenAppendMessage_thenMessageWithForegroundColorAdded() {
    SnackbarBuilder builder = createBuilder();

    builder.appendMessage("message", Color.RED);

    assertThatMessagesWithColorsAppended(builder, "message", Color.RED);
  }

  @Test
  public void givenMessageAlreadyAppended_whenAppendMessageWithColor_thenMessagesWithForegroundColorsAdded() {
    SnackbarBuilder builder = createBuilder();
    builder.appendMessage("first", Color.BLACK);

    builder.appendMessage("second", Color.MAGENTA);

    assertThatMessagesWithColorsAppended(builder, "firstsecond", Color.BLACK, Color.MAGENTA);
  }

  @Test
  public void whenAppendMessageResWithColorRes_thenMessageWithForegroundColorAdded() {
    SnackbarBuilder builder = createBuilder();

    builder.appendMessage(R.string.snackbarbuilder_action_undo, R.color.snackbarbuilder_default_message);

    assertThatMessagesWithColorsAppended(builder, "Undo", Color.WHITE);
  }

  private void assertThatMessagesWithColorsAppended(SnackbarBuilder builder, String expected, int... colors) {
    int length = expected.length();
    assertThat(builder.appendMessages.subSequence(0, length).toString()).isEqualTo(expected);
    ForegroundColorSpan[] spans = builder.appendMessages
        .getSpans(0, length, ForegroundColorSpan.class);
    assertThat(spans.length).isEqualTo(colors.length);
    for (int i = 0; i < colors.length; i++) {
      assertThat(spans[i].getForegroundColor()).isEqualTo(colors[i]);
    }
  }

  @Test
  public void whenDuration_thenDurationSet() {
    SnackbarBuilder builder = createBuilder();

    builder.duration(Snackbar.LENGTH_INDEFINITE);

    assertThat(builder.duration).isEqualTo(Snackbar.LENGTH_INDEFINITE);
  }

  @Test
  public void whenActionTextColorRes_thenActionTextColorSet() {
    SnackbarBuilder builder = createBuilder();

    builder.actionTextColorRes(R.color.snackbarbuilder_default_message);

    assertThat(builder.actionTextColor).isEqualTo(Color.WHITE);
  }

  @Test
  public void whenActionTextColor_thenActionTextColorSet() {
    SnackbarBuilder builder = createBuilder();

    builder.actionTextColor(0xFF333333);

    assertThat(builder.actionTextColor).isEqualTo(0xFF333333);
  }

  @Test
  public void whenActionTextWithString_thenActionTextSet() {
    SnackbarBuilder builder = createBuilder();

    builder.actionText("text");

    assertThat(builder.actionText).isEqualTo("text");
  }

  @Test
  public void whenActionTextWithStringResource_thenActionTextSet() {
    SnackbarBuilder builder = createBuilder();

    builder.actionText(R.string.snackbarbuilder_action_undo);

    assertThat(builder.actionText).isEqualTo("Undo");
  }

  @Test
  public void whenBackgroundColorRes_thenBackgroundColorSet() {
    SnackbarBuilder builder = createBuilder();

    builder.backgroundColorRes(R.color.snackbarbuilder_default_message);

    assertThat(builder.backgroundColor).isEqualTo(Color.WHITE);
  }

  @Test
  public void whenBackgroundColor_thenBackgroundColorSet() {
    SnackbarBuilder builder = createBuilder();

    builder.backgroundColor(0xFF333333);

    assertThat(builder.backgroundColor).isEqualTo(0xFF333333);
  }

  @Test
  public void whenActionClickListener_thenActionClickListenerSet() {
    SnackbarBuilder builder = createBuilder();
    OnClickListener click = new OnClickListener() {
      @Override
      public void onClick(View view) {
        //Click
      }
    };

    builder.actionClickListener(click);

    assertThat(builder.actionClickListener).isEqualTo(click);
  }

  @Test
  public void whenCallback_thenCallbackSet() {
    SnackbarBuilder builder = createBuilder();
    Callback callback = new Callback() {
      @Override
      public void onDismissed(Snackbar snackbar, int event) {
        super.onDismissed(snackbar, event);
      }
    };

    builder.callback(callback);

    assertThat(builder.callbacks).containsOnly(callback);
  }

  @Test
  public void whenSnackbarCallback_thenSnackbarCallbackSet() {
    SnackbarBuilder builder = createBuilder();
    SnackbarCallback callback = new SnackbarCallback() {
    };

    builder.snackbarCallback(callback);

    assertThat(builder.callbacks).containsOnly(callback);
  }

  @Test
  public void whenShowCallback_thenCallbackSet() {
    SnackbarBuilder builder = createBuilder();
    SnackbarShowCallback callback = mock(SnackbarShowCallback.class);

    builder.showCallback(callback);
    builder.callbacks.get(0).onShown(snackbar);

    verify(callback).onSnackbarShown(snackbar);
  }

  @Test
  public void whenDismissCallback_thenCallbackSet() {
    SnackbarBuilder builder = createBuilder();
    SnackbarDismissCallback callback = mock(SnackbarDismissCallback.class);

    builder.dismissCallback(callback);
    builder.callbacks.get(0).onDismissed(snackbar, 0);

    verify(callback).onSnackbarDismissed(snackbar, 0);
  }

  @Test
  public void whenActionDismissCallback_thenCallbackSet() {
    SnackbarBuilder builder = createBuilder();
    SnackbarActionDismissCallback callback = mock(SnackbarActionDismissCallback.class);

    builder.actionDismissCallback(callback);
    builder.callbacks.get(0).onDismissed(snackbar, SnackbarCallback.DISMISS_EVENT_ACTION);

    verify(callback).onSnackbarActionPressed(snackbar);
  }

  @Test
  public void whenSwipeDismissCallback_thenCallbackSet() {
    SnackbarBuilder builder = createBuilder();
    SnackbarSwipeDismissCallback callback = mock(SnackbarSwipeDismissCallback.class);

    builder.swipeDismissCallback(callback);
    builder.callbacks.get(0).onDismissed(snackbar, SnackbarCallback.DISMISS_EVENT_SWIPE);

    verify(callback).onSnackbarSwiped(snackbar);
  }

  @Test
  public void whenTimeoutDismissCallback_thenCallbackSet() {
    SnackbarBuilder builder = createBuilder();
    SnackbarTimeoutDismissCallback callback = mock(SnackbarTimeoutDismissCallback.class);

    builder.timeoutDismissCallback(callback);
    builder.callbacks.get(0).onDismissed(snackbar, SnackbarCallback.DISMISS_EVENT_TIMEOUT);

    verify(callback).onSnackbarTimedOut(snackbar);
  }

  @Test
  public void whenManualDismissCallback_thenCallbackSet() {
    SnackbarBuilder builder = createBuilder();
    SnackbarManualDismissCallback callback = mock(SnackbarManualDismissCallback.class);

    builder.manualDismissCallback(callback);
    builder.callbacks.get(0).onDismissed(snackbar, SnackbarCallback.DISMISS_EVENT_MANUAL);

    verify(callback).onSnackbarManuallyDismissed(snackbar);
  }

  @Test
  public void whenConsecutiveDismissCallback_thenCallbackSet() {
    SnackbarBuilder builder = createBuilder();
    SnackbarConsecutiveDismissCallback callback = mock(SnackbarConsecutiveDismissCallback.class);

    builder.consecutiveDismissCallback(callback);
    builder.callbacks.get(0).onDismissed(snackbar, SnackbarCallback.DISMISS_EVENT_CONSECUTIVE);

    verify(callback).onSnackbarDismissedAfterAnotherShown(snackbar);
  }

  @Test
  public void givenTrue_whenActionAllCaps_thenActionAllCapsTrue() {
    SnackbarBuilder builder = createBuilder();

    builder.actionAllCaps(true);

    assertThat(builder.actionAllCaps).isTrue();
  }

  @Test
  public void givenFalse_whenActionAllCaps_thenActionAllCapsFalse() {
    SnackbarBuilder builder = createBuilder();

    builder.actionAllCaps(false);

    assertThat(builder.actionAllCaps).isFalse();
  }

  @Test
  public void whenIconWithDrawableResource_thenIconSet() {
    SnackbarBuilder builder = createBuilder();
    @DrawableRes int drawableResId = 50;
    getResourceCreator(builder).createMockDrawableResId(drawableResId, drawable);

    builder.icon(drawableResId);

    assertThat(builder.icon).isEqualTo(drawable);
  }

  @Test
  public void whenIconWithDrawable_thenIconSet() {
    SnackbarBuilder builder = createBuilder();
    @DrawableRes int drawableResId = 50;
    getResourceCreator(builder).createMockDrawableResId(drawableResId, drawable);

    builder.icon(drawable);

    assertThat(builder.icon).isEqualTo(drawable);
  }

  @Test
  public void whenIconMargin_thenIconMarinSet() {
    SnackbarBuilder builder = createBuilder();
    int iconMargin = 100;

    builder.iconMargin(iconMargin);

    assertThat(builder.iconMargin).isEqualTo(iconMargin);
  }

  @Test
  public void whenIconMarginRes_thenIconMarinSet() {
    SnackbarBuilder builder = createBuilder();
    int iconMargin = 100;
    @DimenRes int dimenResId = getResourceCreator(builder)
        .getDimensionPixelSize(iconMargin);

    builder.iconMarginRes(dimenResId);

    assertThat(builder.iconMargin).isEqualTo(iconMargin);
  }

  @Test
  public void whenBuildWrapper_thenSnackbarWrapperSetup() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_AppTheme);
    CoordinatorLayout parent = new CoordinatorLayout(RuntimeEnvironment.application);

    SnackbarWrapper wrapper = new SnackbarBuilder(parent)
        .message("message")
        .actionText("action")
        .duration(Snackbar.LENGTH_SHORT)
        .buildWrapper();

    assertThat(wrapper.getText()).isEqualTo("message");
    assertThat(wrapper.getActionText()).isEqualTo("action");
    assertThat(wrapper.getDuration()).isEqualTo(Snackbar.LENGTH_SHORT);
  }

  @Test
  public void givenActionTextNoClickListener_whenBuildWrapper_thenActionTextSet() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_AppTheme);
    CoordinatorLayout parent = new CoordinatorLayout(RuntimeEnvironment.application);

    SnackbarWrapper wrapper = new SnackbarBuilder(parent)
        .actionText("action")
        .buildWrapper();

    assertThat(wrapper.getActionText()).isEqualTo("action");
  }

  @Test
  public void givenActionTextAndClickListener_whenBuildWrapper_thenActionTextAndClickListenerSet() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_AppTheme);
    CoordinatorLayout parent = new CoordinatorLayout(RuntimeEnvironment.application);

    SnackbarWrapper wrapper = new SnackbarBuilder(parent)
        .actionText("action")
        .actionClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            view.setBackgroundColor(Color.RED);
          }
        })
        .buildWrapper();

    assertThat(wrapper.getActionText()).isEqualTo("action");
    View actionView = wrapper.getView().findViewById(R.id.snackbar_action);
    actionView.performClick();
    Assertions.assertThat((ColorDrawable) actionView.getBackground()).hasColor(Color.RED);
  }

  @Test
  @TargetApi(11)
  public void whenBuild_thenSnackbarSetup() {
    int messageTextColor = 0xFF111111;
    int actionTextColor = 0xFF999999;
    int backgroundCOlor = 0xFF777777;
    String message = "message";
    String action = "action";
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_AppTheme);
    CoordinatorLayout parent = new CoordinatorLayout(RuntimeEnvironment.application);

    Snackbar snackbar = new SnackbarBuilder(parent)
        .messageTextColor(messageTextColor)
        .actionTextColor(actionTextColor)
        .message(message)
        .actionText(action)
        .duration(Snackbar.LENGTH_INDEFINITE)
        .backgroundColor(backgroundCOlor)
        .actionAllCaps(false)
        .build();

    assertThat(snackbar.getDuration()).isEqualTo(Snackbar.LENGTH_INDEFINITE);

    Assertions.assertThat((TextView) snackbar.getView().findViewById(R.id.snackbar_text))
        .hasCurrentTextColor(messageTextColor)
        .hasText(message);

    Assertions.assertThat((ColorDrawable) snackbar.getView().getBackground())
        .hasColor(backgroundCOlor);

    Button button = (Button) snackbar.getView().findViewById(R.id.snackbar_action);
    Assertions.assertThat(button).hasCurrentTextColor(actionTextColor);
    button.performClick();
    assertThat(button.getTransformationMethod()).isNull();
  }

  @Test
  public void givenCallback_whenBuild_thenCallbackSet() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_AppTheme);
    CoordinatorLayout parent = new CoordinatorLayout(RuntimeEnvironment.application);

    Snackbar snackbar = new SnackbarBuilder(parent)
        .message("message")
        .actionText("action")
        .duration(Snackbar.LENGTH_SHORT)
        .callback(callback)
        .build();
    snackbar.show();

    snackbar.dismiss();
    verify(callback).onDismissed(snackbar, Callback.DISMISS_EVENT_MANUAL);
  }

  @Test
  public void givenSnackbarCallback_whenBuild_thenSnackbarCallbackSet() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_AppTheme);
    CoordinatorLayout parent = new CoordinatorLayout(RuntimeEnvironment.application);

    Snackbar snackbar = new SnackbarBuilder(parent)
        .message("message")
        .actionText("action")
        .duration(Snackbar.LENGTH_SHORT)
        .snackbarCallback(snackbarCallback)
        .build();
    snackbar.show();

    snackbar.dismiss();
    verify(snackbarCallback).onSnackbarManuallyDismissed(snackbar);
  }

  @Test
  public void givenNotCalledActionAllCaps_whenBuild_thenActionAllCaps() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_AppTheme);
    CoordinatorLayout parent = new CoordinatorLayout(RuntimeEnvironment.application);

    Snackbar snackbar = new SnackbarBuilder(parent)
        .message("message")
        .actionText("action")
        .duration(Snackbar.LENGTH_SHORT)
        .build();
    snackbar.show();

    Button button = (Button) snackbar.getView().findViewById(R.id.snackbar_action);
    assertThat(button.getTransformationMethod()).isNotNull();
  }

  @Test
  public void givenIcon_whenBuild_thenIconAddedToSnackbar() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_AppTheme);
    CoordinatorLayout parent = new CoordinatorLayout(RuntimeEnvironment.application);

    Snackbar snackbar = new SnackbarBuilder(parent)
        .message("messsage")
        .icon(drawable)
        .iconMargin(10)
        .build();

    TextView messageView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
    Assertions.assertThat(messageView)
        .hasCompoundDrawablePadding(10);
    assertThat(messageView.getCompoundDrawables()[0]).isEqualTo(drawable);
  }

  @Test
  public void givenAppendedMessages_whenBuild_thenMessagesAppendedToMainMessage() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_AppTheme);
    CoordinatorLayout parent = new CoordinatorLayout(RuntimeEnvironment.application);

    final Snackbar snackbar = new SnackbarBuilder(parent)
        .message("start")
        .appendMessage("first_added")
        .appendMessage("second_in_blue", Color.BLUE)
        .appendMessage("third_in_dark_grey", Color.DKGRAY)
        .build();

    List<Pair<String, Integer>> expected = new ArrayList<>();
    expected.add(new Pair<>("start", 0));
    expected.add(new Pair<>("first_added", 0));
    expected.add(new Pair<>("second_in_blue", Color.BLUE));
    expected.add(new Pair<>("third_in_dark_grey", Color.DKGRAY));
    SnackbarCustomAssert.assertThat(snackbar).hasMessagesAppended(expected);
  }

  private SnackbarBuilder createBuilder() {
    RuntimeEnvironment.application.setTheme(R.style.TestSnackbarBuilder_AppTheme);
    return new SnackbarBuilder(parentView);
  }

  private MockResourceCreator getResourceCreator(SnackbarBuilder builder) {
    return MockResourceCreator.fromBuilder(builder)
        .withContext(activity)
        .withResources(resources);
  }
}