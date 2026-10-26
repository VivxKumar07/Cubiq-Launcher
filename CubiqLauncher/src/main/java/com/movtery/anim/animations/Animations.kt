package com.movtery.anim.animations

import com.movtery.anim.animations.bounce.BounceEnlargeAnimator
import com.movtery.anim.animations.bounce.BounceInDownAnimator
import com.movtery.anim.animations.bounce.BounceInLeftAnimator
import com.movtery.anim.animations.bounce.BounceInRightAnimator
import com.movtery.anim.animations.bounce.BounceInUpAnimator
import com.movtery.anim.animations.bounce.BounceShrinkAnimator
import com.movtery.anim.animations.fade.FadeInAnimator
import com.movtery.anim.animations.fade.FadeInDownAnimator
import com.movtery.anim.animations.fade.FadeInLeftAnimator
import com.movtery.anim.animations.fade.FadeInRightAnimator
import com.movtery.anim.animations.fade.FadeInUpAnimator
import com.movtery.anim.animations.fade.FadeOutAnimator
import com.movtery.anim.animations.fade.FadeOutDownAnimator
import com.movtery.anim.animations.fade.FadeOutLeftAnimator
import com.movtery.anim.animations.fade.FadeOutRightAnimator
import com.movtery.anim.animations.fade.FadeOutUpAnimator
import com.movtery.anim.animations.other.PulseAnimator
import com.movtery.anim.animations.other.ShakeAnimator
import com.movtery.anim.animations.other.WobbleAnimator
import com.movtery.anim.animations.slide.SlideInDownAnimator
import com.movtery.anim.animations.slide.SlideInLeftAnimator
import com.movtery.anim.animations.slide.SlideInRightAnimator
import com.movtery.anim.animations.slide.SlideInUpAnimator
import com.movtery.anim.animations.slide.SlideOutDownAnimator
import com.movtery.anim.animations.slide.SlideOutLeftAnimator
import com.movtery.anim.animations.slide.SlideOutRightAnimator
import com.movtery.anim.animations.slide.SlideOutUpAnimator

enum class Animations(val animator: BaseAnimator) {
    //Bounce
    BounceInDown(BounceInDownAnimator()),
    BounceInLeft(BounceInLeftAnimator()),
    BounceInRight(BounceInRightAnimator()),
    BounceInUp(BounceInUpAnimator()),
    BounceEnlarge(BounceEnlargeAnimator()),
    BounceShrink(BounceShrinkAnimator()),

    //Fade in
    FadeIn(FadeInAnimator()),
    FadeInLeft(FadeInLeftAnimator()),
    FadeInRight(FadeInRightAnimator()),
    FadeInUp(FadeInUpAnimator()),
    FadeInDown(FadeInDownAnimator()),

    //Fade out
    FadeOut(FadeOutAnimator()),
    FadeOutLeft(FadeOutLeftAnimator()),
    FadeOutRight(FadeOutRightAnimator()),
    FadeOutUp(FadeOutUpAnimator()),
    FadeOutDown(FadeOutDownAnimator()),

    //Slide in
    SlideInLeft(SlideInLeftAnimator()),
    SlideInRight(SlideInRightAnimator()),
    SlideInUp(SlideInUpAnimator()),
    SlideInDown(SlideInDownAnimator()),

    //Slide out
    SlideOutLeft(SlideOutLeftAnimator()),
    SlideOutRight(SlideOutRightAnimator()),
    SlideOutUp(SlideOutUpAnimator()),
    SlideOutDown(SlideOutDownAnimator()),

    //Other
    Pulse(PulseAnimator()),
    Wobble(WobbleAnimator()),
    Shake(ShakeAnimator())
}