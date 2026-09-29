# Samsung Galaxy A21 support matrix

Galaxy A21 معمولاً با Android 10 عرضه شده و ممکن است بسته به منطقه به نسخه‌های بعدی One UI ارتقا یافته باشد. `minSdk 26` و APIهای محافظت‌شده باعث می‌شوند برنامه روی Android 8+ اجرا شود، ولی Thermal Status عمومی از Android 10 (API 29) در دسترس است.

| قابلیت | وضعیت واقعی | منبع / رفتار fallback |
|---|---|---|
| RAM آزاد و کل | پشتیبانی‌شده | `ActivityManager.MemoryInfo`; اگر سرویس پاسخ ندهد Unavailable |
| Battery level / charging | پشتیبانی‌شده | sticky `ACTION_BATTERY_CHANGED`؛ بدون receiver دائمی |
| دمای باتری | معمولاً پشتیبانی‌شده | `BatteryManager.EXTRA_TEMPERATURE`; سنسور نامعتبر نمایش داده نمی‌شود |
| Battery Saver | پشتیبانی‌شده | `PowerManager.isPowerSaveMode` |
| Thermal pressure | Android 10+ و وابسته به OEM | `PowerManager.currentThermalStatus` و listener عمومی؛ در غیر این صورت Not supported |
| دمای CPU/GPU | پشتیبانی عمومی ندارد | جعل نمی‌شود؛ فقط thermal status و دمای باتری نمایش داده می‌شود |
| CPU core count | پشتیبانی‌شده | `Runtime.availableProcessors()` |
| CPU usage کل سیستم | محدودشده | Android API عمومی قابل اتکا ندارد؛ UI این محدودیت را نشان می‌دهد |
| تشخیص COD Mobile | پشتیبانی‌شده | چهار package رسمی و declaration محدود `<queries>` |
| اجرای بازی | در صورت نصب | فقط launch intent رسمی PackageManager |
| FPS / frame time بازی | پشتیبانی عمومی ندارد | مقدار `—` و پیام واضح؛ هیچ تخمینی تولید نمی‌شود |
| Kill کردن همه برنامه‌ها | غیرمجاز/بی‌اثر در Android جدید | انجام نمی‌شود؛ کاربر در صورت نیاز از Recents استفاده می‌کند |
| تغییر clock/governor CPU/GPU | بدون روت غیرممکن | انجام نمی‌شود |
| Overclock یا خاموش‌کردن throttling | ناامن و غیرمجاز | انجام نمی‌شود؛ Extreme محافظ سخت‌گیرانه‌تری دارد |
| Game Mode سامسونگ | API عمومی پایدار برای برنامه ثالث ندارد | تغییر داده نمی‌شود؛ برنامه crash نمی‌کند |
| Input lag مستقیم | کنترل عمومی ندارد | فقط رقابت منابع خود Optimizer بعد از Launch حذف می‌شود |

## رفتار حرارتی

- `NONE` و `LIGHT`: وضعیت عادی.
- `MODERATE`: هشدار کاهش تنظیمات گرافیکی/روشنایی.
- `SEVERE`: Extreme متوقف می‌شود؛ پروفایل‌های دیگر هشدار جدی می‌دهند.
- `CRITICAL`، `EMERGENCY` و `SHUTDOWN`: Launch در همه پروفایل‌ها متوقف می‌شود.
- دمای باتری 45°C یا بیشتر: Extreme متوقف می‌شود و در بقیه پروفایل‌ها هشدار داده می‌شود.

این حدها جایگزین کنترل حرارتی خود Samsung نیستند و محافظ سیستم همیشه مرجع نهایی است.

## تنظیم پیشنهادی عملی برای A21

- برای بازی طولانی **Performance** و گرافیک Low/Medium داخل خود بازی معمولاً پایدارتر از Extreme است.
- در زمان گرم‌شدن، روشنایی را کم کنید، هنگام شارژ بازی نکنید و گوشی را روی سطحی با گردش هوا قرار دهید.
- Battery Saver می‌تواند عملکرد پایدار را محدود کند؛ فقط با تصمیم کاربر در Android Settings خاموش شود.
- نرخ فریم را فقط از گزینه‌های رسمی داخل بازی انتخاب کنید؛ گزینه‌ای که سخت‌افزار/بازی ارائه نمی‌کند قابل بازکردن امن نیست.
