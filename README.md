# A21 Game Optimizer

یک بهینه‌ساز **واقعی، سبک، آفلاین و بدون روت** برای اجرای Call of Duty: Mobile روی Samsung Galaxy A21 و گوشی‌های اندرویدی مشابه.

این پروژه FPS جعلی نمایش نمی‌دهد، بازی را دستکاری نمی‌کند و وعده غیرواقعی افزایش دوبرابری FPS نمی‌دهد. کاری که انجام می‌دهد یک **pre-flight check** دقیق و کم‌مصرف است: RAM، باتری، Battery Saver و فشار حرارتی عمومی اندروید را بررسی می‌کند، نسخه رسمی منطقه‌ای بازی را تشخیص می‌دهد، هشدار قابل‌اقدام می‌دهد و پس از اجرای بازی خودش را می‌بندد تا منبعی اشغال نکند.

## قابلیت‌های اصلی

- رابط مدرن Kotlin + Jetpack Compose + Material 3 با پشتیبانی خودکار از تم روشن/تیره
- رابط کامل فارسی و انگلیسی مطابق زبان دستگاه
- سه پروفایل Balanced، Performance و Extreme با محافظ حرارتی واقعی
- خواندن RAM از `ActivityManager.MemoryInfo`
- خواندن شارژ، دما و وضعیت باتری از API استاندارد `BatteryManager`
- خواندن `PowerManager.getCurrentThermalStatus()` در Android 10+
- تشخیص نسخه‌های رسمی Global، Garena، Vietnam و China با package visibility محدود
- مسدودکردن Extreme در گرمای خطرناک؛ هیچ محافظ سیستمی دور زده نمی‌شود
- لینک امن به تنظیمات Battery Saver، Display، DND و Battery Optimization
- بدون سرویس پس‌زمینه، Overlay، Polling، اینترنت، تبلیغات، آنالیتیکس یا مجوز خطرناک
- بستن کامل Task بهینه‌ساز بلافاصله بعد از اجرای بازی
- Fallback برای API/سرویس پشتیبانی‌نشده، نصب‌نبودن بازی و تغییر رفتار One UI

## نصب APK آماده

1. فایل زیر را به گوشی منتقل کنید:
   `artifacts/A21GameOptimizer-v1.0.0-release-dev-signed.apk`
2. SHA-256 آن را با فایل کنار APK مقایسه کنید.
3. در صورت درخواست Android، فقط برای File Manager مورد استفاده اجازه نصب از منبع ناشناس را موقتاً فعال کنید.
4. APK را نصب و اجرا کنید.

APK مخزن یک build تأییدشده و **development-signed** برای نصب مستقیم است. برای انتشار عمومی باید برنامه با کلید خصوصی مالک امضا شود. کلید توسعه هیچ دسترسی ویژه‌ای در گوشی ایجاد نمی‌کند؛ این تفکیک فقط مربوط به هویت و به‌روزرسانی امن ناشر است.

## استفاده

1. نسخه منطقه‌ای COD Mobile را در بخش Game launcher انتخاب کنید. نقطه سبز یعنی Package رسمی نصب شده است.
2. پروفایل را انتخاب کنید:
   - **Balanced:** راهنمایی سازگارتر با باتری.
   - **Performance:** حساسیت بیشتر به رقابت RAM و Battery Saver.
   - **Extreme:** محافظ حرارتی سخت‌گیرانه؛ در فشار حرارتی Severe یا دمای بالای باتری اجرا را متوقف می‌کند.
3. `BOOST GAME` را بزنید.
4. گزارش پیش از اجرا را بخوانید. هشدارها را رفع کنید یا در شرایط امن `Launch & close optimizer` را بزنید.
5. برنامه بازی رسمی را باز و Task خودش را حذف می‌کند. هیچ مانیتور یا Overlay در پس‌زمینه باقی نمی‌ماند.

## FPS Monitor چرا عدد نشان نمی‌دهد؟

یک برنامه عادی و بدون روت نمی‌تواند FPS یا frame time برنامه دیگری را با API عمومی Android بخواند. بنابراین صفحه Monitor صریحاً `FPS data unavailable through Android APIs` نشان می‌دهد و فقط داده‌های واقعاً قابل دسترس را نمایش می‌دهد. تولید عدد تخمینی می‌توانست گمراه‌کننده باشد و عمداً انجام نشده است.

## ساخت پروژه

پیش‌نیازها:

- JDK 17
- Android SDK Platform 36 و Build Tools
- Gradle 8.14.5 (یا Gradle Wrapper موجود در مخزن)

```bash
./gradlew testDebugUnitTest lintRelease assembleAndroidTest assembleDebug
```

برای Release خصوصی، `keystore.properties.example` را به `keystore.properties` کپی کنید و مسیر/رمز کلید خودتان را قرار دهید؛ فایل واقعی و keystore در Git ignore شده‌اند:

```bash
./gradlew clean testDebugUnitTest lintRelease assembleRelease
```

APK در `app/build/outputs/apk/release/` ساخته می‌شود. اگر اطلاعات signing ارائه نشود، variant ریلیز عمداً unsigned خواهد بود؛ build آماده مخزن توسط workflow ایزوله امضا و با `apksigner` بررسی شده است.

## معماری

- **UI:** Compose screens/components، تک Activity و state یک‌طرفه
- **State:** `MainViewModel` + `StateFlow`/`SharedFlow`
- **Data:** repositoryهای مستقل برای Device Status، تشخیص بازی و preference محلی
- **Domain:** `BoostPlanner` کاملاً تست‌پذیر و مستقل از Android
- **Lifecycle:** thermal listener فقط در طول عمر ViewModel؛ بدون background service

جزئیات بیشتر: [Galaxy A21 support matrix](docs/GALAXY_A21_SUPPORT.md) و [QA report](docs/QA_REPORT.md).

## امنیت و Anti-Cheat

این برنامه APK یا فایل‌های COD Mobile را تغییر نمی‌دهد، حافظه تزریق نمی‌کند، ترافیک را رهگیری نمی‌کند، Anti-Cheat را دور نمی‌زند و Login دریافت نمی‌کند. Manifest هیچ permission خطرناک یا Internet permission ندارد. [سیاست حریم خصوصی](PRIVACY.md)

## محدودیت صادقانه

Android جدید به یک optimizer ثالث اجازه kill عمومی برنامه‌ها، تغییر governor/clock پردازنده، کنترل GPU، غیرفعال‌کردن thermal throttling یا تغییر FPS cap بازی را نمی‌دهد. این پروژه به‌جای جعل این قابلیت‌ها روی کاهش سربار خودش، تشخیص شرایط نامناسب و Launch امن تمرکز می‌کند.

## License

MIT
