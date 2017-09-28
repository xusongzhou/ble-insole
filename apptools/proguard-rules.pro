# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\WorkApp\Android\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#指定代码的压缩级别
-optimizationpasses 5    
#包明不混合大小写
-dontusemixedcaseclassnames    
#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses    
#不优化输入的类文件
-dontoptimize    
#预校验
-dontpreverify
# 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#保护注解
-keepattributes *Annotation*
# 保持哪些类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
#如果有引用v4包可以添加下面这行
-keep public class * extends android.support.v4.app.Fragment
#忽略警告
-ignorewarning
#忽略第三方jar包
-keep class com.amap.api.** {*;}
-keep class com.autonavi.amap.mapcore.** {*;}
-keep class com.autonavi.aps.amapapi.model.** {*;}
-keep class com.loc.** {*;}
-keep class de.greenrobot.dao.** {*;}
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties
-keep class com.tencent.** {*;}
-keep class com.github.mikephil.charting.** {*;}
-keep class com.nineoldandroids.** {*;}
-keep class com.google.zxing.** {*;}
-keep class com.zxing.android.** {*;}

-keep class com.advanpro.ansobuy.db.DaoMaster
-keep class com.advanpro.ansobuy.db.DaoSession

-keep public class * extends android.view.View {
	public <init>(android.content.Context);
	public <init>(android.content.Context, android.util.AttributeSet);
	public <init>(android.content.Context, android.util.AttributeSet, int);
	public void set*(...);
}

#保持 native 方法不被混淆
-keepclasseswithmembernames class * {
	native <methods>;
}

#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
	public <init>(android.content.Context, android.util.AttributeSet);
}

#保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable

#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
	static final long serialVersionUID;
	private static final java.io.ObjectStreamField[] serialPersistentFields;
	!static !transient <fields>;
	!private <fields>;
	!private <methods>;
	private void writeObject(java.io.ObjectOutputStream);
	private void readObject(java.io.ObjectInputStream);
	java.lang.Object writeReplace();
	java.lang.Object readResolve();
}
#不混淆资源类
-keepclassmembers class **.R$* {
	public static <fields>;
}