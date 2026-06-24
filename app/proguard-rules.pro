# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class getChannelName to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file getChannelName.
#-renamesourcefileattribute SourceFile

# no case mis package. aA Aa
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-ignorewarnings
-dontwarn androidx.**
-dontwarn okhttp3.**
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
# keep native method
-keepclasseswithmembernames class * {
    native <methods>;
}
#keep android sdk
-keep class com.google.android.material.** {*;}
-keep class android.** {*;}
-keep public class * extends android.**
-keep interface android.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-keep class org.** { *; }
-keep class * implements androidx.viewbinding.ViewBinding { *; }
-keep class **.R$* {*;}
-keep class android.net.ConnectivityManager { *; }
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }
###############################################
#keep Interceptor
-keep class * implements acquire.base.chain.Interceptor
#keep json package
-keep class acquire.core.bean.json.** {*;}
### evetnbus
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
### newland libraries
#keep newland nsdk
-keep class android.newland.** {*;}
-keep class com.newland.nsdk.** {*;}
-keep class com.newland.me.** {*;}
#keep newland emvL3
-keep class com.newland.sdk.emvl3.** {*;}

-dontwarn org.xmlpull.v1.**

# Keep custom model classes
-keep class acquire.app.brac.models.** { *; }
-keep class acquire.core.model.** { *; }
-keep class acquire.app.fragment.main.menu.** { *; }

# Keep Gson type info
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Gson
-keep class com.google.gson.** { *; }
# Keep generic type information
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod


#--
# Keep TransTag constants
-keep class acquire.core.constant.TransTag {
    public static final java.lang.String *;
}

# Keep all constant classes
-keep class acquire.core.constant.** { *; }

# Transaction Tag constants
#-keep class acquire.core.constant.TransTag {
#    <fields>;
#}
-keepclassmembers class acquire.core.constant.TransTag {
    public static final java.lang.String *;
}
# Constants / Tags / Intent Keys / Bundle Keys
-keep class acquire.core.constant.** { *; }

# Main Menu models
-keep class acquire.app.fragment.main.menu.** { *; }

# Keep PubBean model and all members
-keep class acquire.core.bean.PubBean {
    *;
}

# Keep fields/methods used by reflection/serialization
-keepclassmembers class acquire.core.bean.PubBean {
    <fields>;
    <methods>;
}

# Keep BindTag annotation runtime access
-keep @interface acquire.core.BindTag

# Keep classes with BindTag annotated fields
-keepclassmembers class * {
    @acquire.core.BindTag <fields>;
}

# Gson model keep
-keep class acquire.core.bean.PubBean {
    <fields>;
}


#---
#-keep class acquire.core.trans.impl.** {
#    *;
#}

# Keep TestTxn transaction class
#-keep class acquire.core.trans.AbstractTrans { *; }
-keep class acquire.core.trans.impl.about.About { *; }
-keep class acquire.core.trans.impl.auth_complete.AuthComplete { *; }
-keep class acquire.core.trans.impl.balance.Balance { *; }
-keep class acquire.core.trans.impl.test_txn.TestTxn { *; }
-keep class acquire.core.trans.impl.sale.Sale { *; }
-keep class acquire.core.trans.impl.void_sale.VoidSale { *; }
-keep class acquire.core.trans.impl.installment.Installment { *; }
-keep class acquire.core.trans.impl.void_installment.VoidInstallment { *; }
-keep class acquire.core.trans.impl.installment.InstallmentMenu { *; }
-keep class acquire.core.trans.impl.preauth.PreAuth { *; }
-keep class acquire.core.trans.impl.preauth.PreAuthMenu { *; }
-keep class acquire.core.trans.impl.void_preauth.VoidPreAuth { *; }
-keep class acquire.core.trans.impl.settle.Settle { *; }
-keep class acquire.core.trans.impl.log_on.LogOn { *; }
-keep class acquire.core.trans.impl.log_on_installment.InstallmentLogOn { *; }
-keep class acquire.core.trans.impl.settings.Settings { *; }
-keep class acquire.settings.fragment.brac_setting.BracMainSettingFragment { *; }
-keep class acquire.settings.fragment.brac_setting.BracSettingThirdSettingMenuFragment { *; }


#---
#-keepattributes Signature
#-keepattributes InnerClasses
#-keepattributes EnclosingMethod
# Keep all ViewBinding classes and inflate/bind methods
#-keep class * implements androidx.viewbinding.ViewBinding {
#    public static *** inflate(android.view.LayoutInflater);
#    public static *** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
#    public static *** bind(android.view.View);
#}
#
#-keepclassmembers class ** implements androidx.viewbinding.ViewBinding {
#    public static ** bind(***);
#    public static ** inflate(***);
#}
#
#-keep class acquire.base.widget.databinding.* {
#    public static ** inflate( ** );
#    public static ** bind( ** );
#}


# Keep ViewBinding classes
-keep class * implements androidx.viewbinding.ViewBinding { *; }

# Keep ResultFragment inner classes
-keep class acquire.core.fragment.result.ResultFragment$* { *; }

# Keep BaseBindingRecyclerAdapter generic info
-keep class acquire.base.widget.BaseBindingRecyclerAdapter { *; }

# Keep all subclasses of BaseBindingRecyclerAdapter
-keep class * extends acquire.base.widget.BaseBindingRecyclerAdapter { *; }