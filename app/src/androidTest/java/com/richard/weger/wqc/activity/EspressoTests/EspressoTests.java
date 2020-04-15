package com.richard.weger.wqc.activity.EspressoTests;

import androidx.test.rule.ActivityTestRule;

import com.google.zxing.Result;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.activity.WelcomeActivity;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.richard.weger.wqc.activity.EspressoTests.util.ViewUtils.getString;
import static com.richard.weger.wqc.activity.EspressoTests.util.ViewUtils.waitId;
import static com.richard.weger.wqc.activity.EspressoTests.util.ViewUtils.waitText;
import static com.richard.weger.wqc.result.ErrorResult.ErrorCode.QR_TRANSLATION_FAILED;
import static org.hamcrest.CoreMatchers.containsString;

public class EspressoTests {

    private String sampleProjectInfo = "17-1-435";
    private String validQrSample = "\\" + sampleProjectInfo + "_Z_1_T_1";

    @Rule
    public ActivityTestRule<WelcomeActivity> mActivityRule = new ActivityTestRule<>(WelcomeActivity.class, false, true);

    @Test
    public void testGeneralPictureCapture() {

        onView(isRoot()).perform(waitId(R.id.tvVersion, 5000));
        onView(withId(R.id.tvVersion)).check(matches(isDisplayed()));
        Result result = new Result(validQrSample, null, null, null);
        mActivityRule.getActivity().runOnUiThread(() -> mActivityRule.getActivity().handleResult(result));
        onView(isRoot()).perform(waitId(R.id.tvProjectInfo, 10000));
        onView(withId(R.id.tvProjectInfo)).check(matches(withText(containsString(sampleProjectInfo))));
        onView(withId(R.id.btnCustomPictures)).perform(click());
        onView(withId(R.id.takeButton)).perform(click());
        onView(withId(R.id.takeButton)).perform(click());
        onView(isRoot()).perform(waitText("1", 7000));
        pressBack();
        onView(isRoot()).perform(waitId(R.id.uploadButton, 7000));
        onView(withId(R.id.uploadButton)).check(matches(isDisplayed()));
        onView(withId(R.id.uploadButton)).perform(click());
        onView(withText(getString(mActivityRule, R.string.yesTAG))).perform(click());
        onView(withText(getString(mActivityRule, R.string.okTag))).perform(click());
        onView(withText(getString(mActivityRule, R.string.picturesListTag))).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidQrCode() {
        onView(isRoot()).perform(waitId(R.id.tvVersion, 5000));
        onView(withId(R.id.tvVersion)).check(matches(isDisplayed()));
        Result result = new Result("123", null, null, null);
        mActivityRule.getActivity().runOnUiThread(() -> mActivityRule.getActivity().handleResult(result));
        onView(withText(containsString(QR_TRANSLATION_FAILED.toString()))).inRoot(isDialog()).check(matches(isDisplayed()));
    }

}
