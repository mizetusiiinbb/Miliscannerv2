# Miliconfig IPv6 Scanner Pro 🛡️🔍

![Android CI](https://img.shields.io/badge/Build-GitHub_Actions-brightgreen?logo=github-actions)
![Platform](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-blue?logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin_1.9.23-purple?logo=kotlin)
![UI](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-emerald)
![Brand](https://img.shields.io/badge/Brand-@miliconfig-00E676)
![Developer](https://img.shields.io/badge/Developer-Milad_Jahani-blueviolet)

**Miliconfig IPv6 Scanner Pro** is a high-performance native Android auditing and diagnostic utility engineered specifically for IPv6 TCP connectivity testing and port assessment across authorized targets. Built with Kotlin, Jetpack Compose Material 3, Kotlin Coroutines, and Room local database with a dark glassmorphism design.

---

## فهرست مطالب (Table of Contents)
1. [ویژگی‌های کلیدی نرم‌افزار (Key Features)](#ویژگیهای-کلیدی-نرم‌افزار)
2. [حفاظت‌ها و تدابیر فنی (Security & Technical Safeguards)](#حفاظتها-و-تدابیر-فنی)
3. [راهنمای جامع گام‌به‌گام برای کاربران گیت‌هاب (Persian GitHub Guide)](#راهنمای-جامع-گام‌به‌گام-برای-کاربران-گیت‌هاب)
   - [گام اول: ساخت ریپازیتوری در گیت‌هاب](#گام-اول-ساخت-ریپازیتوری-در-گیت‌هاب)
   - [گام دوم: بارگذاری کلیه فایل‌های پروژه و فایل‌های مخفی](#گام-دوم-بارگذاری-کلیه-فایلهای-پروژه-و-فایلهای-مخفی)
   - [گام سوم: باز کردن تب Actions و نظارت بر ساخت خودکار](#گام-سوم-باز-کردن-تب-actions-و-نظارت-بر-ساخت-خودکار)
   - [گام چهارم: دانلود آرتیفکت APK کامپایل شده](#گام-چهارم-دانلود-آرتیفکت-apk-کامپایل-شده)
   - [گام پنجم: استخراج فایل ZIP و نصب APK روی موبایل](#گام-پنجم-استخراج-فایل-zip-و-نصب-apk-روی-موبایل)
   - [گام ششم: مشاهده لاگ‌های بیلد و رفع اشکال خطاهای رایج](#گام-ششم-مشاهده-لاگهای-بیلد-و-رفع-اشکال-خطاهای-رایج)
4. [معماری پروژه (Project Architecture)](#معماری-پروژه)
5. [ارتباط با ما و کانال میلی‌کانفیگ](#ارتباط-با-ما-و-کانال-میلیکانفیگ)

---

## ویژگی‌های کلیدی نرم‌افزار

- **طراحی مدرن بلور و شیشه‌ای (Dark Glassmorphism UI):** تم تیره اختصاصی با پس‌زمینه Obsidian و لهجه‌های سبز نئونی زنده (`#00E676`) و کارت‌های ترنسپرنت شیشه‌ای.
- **پشتیبانی کامل دو زبانه و راست‌چین (Persian RTL & English LTR):** بومی‌سازی کامل زبان فارسی با فونت و جانمایی خوانا در تمامی بخش‌های برنامه.
- **تست واقعی سوکت TCP (Real Socket Probing):** تمامی نتایج حاصل از ارتباط زنده لایه ۴ شبکه بوده و نتایج به هیچ عنوان شبیه‌سازی یا ساختگی نیستند.
- **حالت‌های چندگانه اسکن هدف:**
  - هدف تکی (Single IPv6 Address)
  - بازه و ساب‌نت شبکه بر اساس CIDR
  - بارگذاری لیست دسته‌ای اهداف به صورت متنی یا فایل TXT/CSV
- **پریست‌های پورت استاندارد و سفارشی:** وب (80, 443, 8080, 8443)، ریموت/SSH (22, 2222, 3389)، دی‌ان‌اس (53, 853)، ایمیل (25, 465, 587, 995)، وی‌پی‌ان (443, 1194, 51820) و بازه‌های سفارشی.
- **وضعیت‌های دقیق نتیجه اسکن:**
  - `OPEN` (پورت باز و آماده اتصال)
  - `REFUSED` (دریافت بسته TCP RST و رد ارتباط)
  - `TIMEOUT` (عدم دریافت پاسخ در مهلت زمانی تعیین‌شده)
  - `UNREACHABLE` (غیرقابل مسیریابی یا قطعی لایه شبکه)
  - `UNKNOWN` (سایر خطاهای سیستم‌عامل)
- **ابزار عیب‌یابی شبکه IPv6 (Diagnostics):** نمایش آدرس‌های محلی و سراسری دستگاه، لیست کارت‌های شبکه فعال و ارسال پروب زنده‌بودن اینترنت IPv6 به سرورهای کلودفلر و گوگل با سنجش تاخیر بر حسب میلی‌ثانیه.
- **پایگاه‌داده محلی تاریخچه (Room Database):** ثبت تمامی سشن‌های اسکن با زمان، اهداف و جزییات در حافظه داخلی دستگاه.
- **خروجی چندفرمت:** ذخیره و اشتراک‌گذاری نتایج در فرمت‌های CSV، JSON و گزارش متنی ساختاریافته (TXT).

---

## حفاظت‌ها و تدابیر فنی

1. **ممنوعیت اسکن فراگیر رنج‌های بزرگ (Large-Prefix Safeguard):** اسکن رنج‌های بزرگ IPv6 نظیر `/64` (شامل ۱۸ کینتیلیون آدرس) از نظر محاسباتی و پهنای باند فاجعه‌بار است. نرم‌افزار دارای گیت امنیتی بوده و اجازه اسکن رنج‌های وسیع‌تر از `/120` (حداکثر ۲۵۶ آدرس) را نمی‌دهد.
2. **مدیریت همزمانی پایدار (Bounded Concurrency):** اسکن از طریق سِمافورهای مدیریت‌شده در Coroutines انجام می‌شود تا منابع حافظه و صف سوکت‌های سیستم‌عامل اندروید اشباع نگردد.
3. **حفظ کامل حریم خصوصی آفلاین:** هیچ اطلاعات، آی‌پی، یا لاگی از دستگاه به سرورهای خارجی ارسال نمی‌شود.

---

## راهنمای جامع گام‌به‌گام برای کاربران گیت‌هاب

کاربران گرامی بدون نیاز به نصب نرم‌افزار سنگین **Android Studio** یا ابزارهای پیچیده کامپایل بر روی رایانه خود، می‌توانند فایل APK نهایی و قابل نصب را مستقیماً از طریق سرویس خودکار **GitHub Actions** دریافت نمایند. مراحل زیر را به ترتیب انجام دهید:

### گام اول: ساخت ریپازیتوری در گیت‌هاب
1. وارد حساب کاربری خود در وب‌سایت [GitHub.com](https://github.com) شوید.
2. از گوشه بالا سمت راست، بر روی آیکون **+** کلیک کرده و گزینه **New repository** را انتخاب کنید.
3. نام ریپازیتوری را مثلاً `miliconfig-ipv6-scanner` وارد کنید.
4. دسترسی را روی **Public** یا **Private** قرار دهید.
5. نیازی به تیک زدن گزینه‌های *Add a README file* یا *.gitignore* نیست (چون این فایل‌ها در پروژه آماده شده‌اند).
6. بر روی دکمه سبز رنگ **Create repository** کلیک کنید.

---

### گام دوم: بارگذاری کلیه فایل‌های پروژه و فایل‌های مخفی
پروژه آماده‌شده شامل پوشه‌ها و فایل‌های مهمی است که برای بیلد الزامی هستند.

#### روش الف) با استفاده از دستورات Git (سریع‌ترین و مطمئن‌ترین روش):
در پوشه محلی پروژه دستورات زیر را در ترمینال یا خط فرمان اجرا کنید:
```bash
git init
git add .
git commit -m "Initial commit - Miliconfig IPv6 Scanner Pro"
git branch -M main
git remote add origin https://github.com/USERNAME/miliconfig-ipv6-scanner.git
git push -u origin main
```
*(به جای `USERNAME`، نام کاربری گیت‌هاب خود را قرار دهید)*

#### روش ب) بارگذاری از طریق وب‌سایت گیت‌هاب:
اگر از محیط وب استفاده می‌کنید، بر روی دکمه **Upload files** در صفحه مخزن کلیک نمایید. **نکته بسیار مهم:** دقت فرمایید که پوشه مخفی `.github/` (که شامل ورک‌فلوهای اکشن است) و پوشه `gradle/wrapper/` حتماً به همراه سایر فایل‌ها آپلود شوند.

---

### گام سوم: باز کردن تب Actions و نظارت بر ساخت خودکار
1. پس از ارسال کدهای پروژه به گیت‌هاب، در صفحه اصلی مخزن به تب **Actions** (در بالای صفحه) مراجعه نمایید.
2. مشاهده خواهید کرد که یک وظیفه خودکار تحت عنوان **Build Miliconfig IPv6 Scanner Pro APK** شروع به اجرا کرده است (با علامت دایره زرد رنگ در حال چرخش).
3. این فرآیند به صورت کاملاً خودکار بر روی یک سرور ابری اوبونتو مجهز به Java 17 و Android SDK اجرا می‌شود، تست‌های واحد را اجرا کرده و فایل نهایی APK را کامپایل می‌کند.
4. مدت زمان بیلد معمولاً حدود ۲ الی ۳ دقیقه است. پس از پایان موفقیت‌آمیز، علامت تیک سبز رنگ (**Green Checkmark**) نمایان خواهد شد.

> **نکته برای اجرای دستی:** در صورتی که تمایل دارید در هر زمان مجدداً بیلد را اجرا نمایید، کافی است در تب **Actions** روی ورک‌فلو کلیک کنید، دکمه **Run workflow** را بزنید و سپس دکمه سبز رنگ را تایید کنید.

---

### گام چهارم: دانلود آرتیفکت APK کامپایل شده
1. بر روی اجرای موفق بیلد (که تیک سبز دارد) کلیک کنید تا وارد صفحه جزئیات اجرا شوید.
2. صفحه را به سمت پایین اسکرول کنید تا به بخش **Artifacts** برسید.
3. در این بخش، فایلی با نام زیر مشاهده خواهید کرد:
   **`Miliconfig-IPv6-Scanner-debug-apk`**
4. بر روی نام این فایل کلیک کنید تا فایل فشرده ZIP دانلود شود.

---

### گام پنجم: استخراج فایل ZIP و نصب APK روی موبایل
1. فایل دانلودی گیت‌هاب به صورت فشرده با فرمت ZIP ذخیره می‌شود. آن را با برنامه مدیریت فایل گوشی (File Manager) یا رایانه استخراج (**Extract**) کنید.
2. فایل با پسوند `.apk` را به گوشی اندرویدی خود منتقل کنید و روی آن ضربه بزنید.
3. **مجوز نصب از منابع ناشناس:** از آنجا که این فایل مستقیماً از سرور ابری گیت‌هاب دانلود شده و از طریق فروشگاه گوگل‌پلی نصب نمی‌شود، سیستم‌عامل اندروید ممکن است پیامی مبنی بر تایید نصب از برنامه مدیریت فایل نمایش دهد. کافی است گزینه **Settings** را زده و سوئیچ **Allow from this source** (اجازه نصب از این منبع) را فعال نمایید.
4. **تذکر امنیتی مهم:** سیستم محافظتی Google Play Protect دستگاه را خاموش نکنید. تنها کافی است در صورت نمایش کادر بررسی، تایید نصب برنامه را انتخاب کنید.

---

### گام ششم: مشاهده لاگ‌های بیلد و رفع اشکال خطاهای رایج
اگر علامت ضربدر قرمز رنگ دیدید:
1. بر روی مرحله خطا کلیک کنید و لاگ خط قرمز رنگ را بررسی فرمایید.
2. **خطای Wrapper:** اطمینان حاصل کنید فایل `gradle/wrapper/gradle-wrapper.jar` و فایل `gradlew` به درستی در مخزن آپلود شده باشند.
3. **خطای مسیر فایل‌ها:** مطمئن شوید ساختار پوشه‌ها تغییر نیافته و فایل `app/build.gradle.kts` در محل صحیح خود قرار دارد.

---

## معماری پروژه (Project Architecture)

```
miliconfig-ipv6-scanner/
├── .github/
│   └── workflows/
│       └── android-build.yml          # GitHub Actions Auto-Build CI/CD Workflow
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar         # Executable Gradle Wrapper Bootstrap
│       └── gradle-wrapper.properties  # Gradle 8.7 Distribution Config
├── app/
│   ├── build.gradle.kts               # Android App Module Config (SDK 34, Compose, Room, KSP)
│   ├── proguard-rules.pro             # Proguard R8 Optimization Rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml    # App Permissions, RTL, Activity Declarations
│       │   ├── java/com/miliconfig/ipv6scanner/
│       │   │   ├── MainActivity.kt    # Root ComponentActivity with Compose Theme
│       │   │   ├── model/             # ScanTarget, PortStatus, ScanResult, NetworkDiagnostics
│       │   │   ├── scanner/           # IPv6Utils, TcpSocketScanner, DiagnosticsEngine
│       │   │   ├── data/              # Room AppDatabase, ScanEntities, Dao, Repository
│       │   │   ├── util/              # ExportHelper (CSV, JSON, TXT reports & Share Intent)
│       │   │   └── ui/                # Compose UI, Dark Glass Theme, Screens, Components
│       │   └── res/
│       │       ├── values/strings.xml # English Strings (LTR)
│       │       ├── values-fa/strings.xml # Persian Strings (RTL)
│       │       ├── values/colors.xml  # Obsidian Dark & Vivid Green Colors
│       │       └── drawable/          # Vector Icons and Adaptive Launcher Icons
│       └── test/java/com/miliconfig/ipv6scanner/
│           ├── IPv6UtilsTest.kt       # Unit tests for IPv6 parsing, CIDR & /64 safeguards
│           └── ExportHelperTest.kt    # Unit tests for CSV, JSON, TXT export formats
├── build.gradle.kts                   # Root Build Script
├── settings.gradle.kts                # Repositories & Plugin Management
├── gradle.properties                  # Memory & AndroidX Settings
├── gradlew                            # POSIX Shell Wrapper Executable
├── gradlew.bat                        # Windows Batch Wrapper Executable
├── .gitignore                         # Android Git Ignore Rules
└── README.md                          # Comprehensive Documentation
```

---

## ارتباط با ما و کانال میلی‌کانفیگ

- **سازنده و توسعه‌دهنده:** میلاد جهانی (Milad Jahani)
- **برند اختصاصی:** `@miliconfig`
- **کانال تلگرام:** [https://t.me/miliconfig](https://t.me/miliconfig)
- **مجوز نرم‌افزار:** انتشار به عنوان پروژه آزاد و متن‌باز جهت حسابرسی امنیت شبکه و اتصالات IPv6.
