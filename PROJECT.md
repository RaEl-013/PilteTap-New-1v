# Plitə tap — Keramo Bazar

Android tətbiqi: kafel/metlax şəklini çəkib kataloqdan ən uyğun modeli tapır.
Platform: **yalnız Android** (WebView + bir `index.html`). Windows versiyası dayandırılıb.

## Texnologiya
- `app/src/main/assets/index.html` — bütün tətbiq (UI + məntiq) tək fayldadır.
- Tanıma: **DINOv2-small** + **CLIP ViT-B/32 (vision)**, ONNX formatında, brauzerdə
  **ONNX Runtime Web (WASM)** ilə işləyir — internetsiz, telefonun öz içində.
- OCR (etiket oxumaq): **Tesseract.js** (WASM), ingilis/latın hərf-rəqəmi oxuyur.
- Saxlama: IndexedDB (kataloq), localStorage (çəkilər, tarixçə).
- Qurma: GitHub Actions (`​.github/workflows/build.yml`) — modelləri, OCR-u, loqo
  ikonunu və sabit imza faylını **özü tikinti zamanı yükləyib yazır**. Repo-nun
  özü kiçik qalır (yalnız kod), böyük fayllar GitHub-un serverində yüklənir.

## Fayl strukturu
```
app/build.gradle.kts              — versiya (GitHub run nömrəsi ilə avtomatik v1,v2,v3…), sabit debug keystore
app/src/main/AndroidManifest.xml  — kamera icazəsi, loqo ikon istinadları
app/src/main/java/.../MainActivity.kt — WebView + fayl seçici + kamera icazəsi körpüsü
app/src/main/assets/index.html    — TƏTBİQİN ÖZÜ (UI+JS, DINOv2/CLIP/OCR/kamera məntiqi)
app/src/main/res/...              — strings, tema, mipmap ikon (Keramo Bazar loqosu)
.github/workflows/build.yml       — CI: model+OCR+ikon+keystore yükləyib APK qurur
debug.keystore                    — ehtiyat nüsxə (CI öz nüsxəsini B64-dən yaradır)
```

## Tamamlanmış xüsusiyyətlər (xronoloji)
1. Əsas axtarış/kataloq UI-ı, kafel/metlax tipi, JPEG seçimi.
2. DINOv2 ilə oxşarlıq axtarışı (rəng/HOG əvvəlcə, sonra əsas model kimi).
3. CLIP əlavə edilib, DINOv2+CLIP+rəng birləşik bal sistemi.
4. Avto-tənzimləmə aləti (kataloqu sınayıb ən yaxşı çəkiləri seçir).
5. Kataloqda redaktə/silmə, çoxlu seçim, axtarış/filtr, "İmtina et".
6. Kameraya çərçivə, canlı keyfiyyət yoxlaması (işıq/kölgə/bulanıqlıq/məsafə).
7. Google Lens tərzi: canlı tanıma (kamera açıq ikən), rəqəmsal yaxınlaşdırma,
   axtarış tarixçəsi.
8. Android tətbiqi (WebView) yaradıldı, GitHub Actions ilə APK qurulur.
9. Ön kamera əvəzinə **arxa kamera** məcburi edildi.
10. Kataloqda kamera ilə birbaşa əlavə (ayrıca "Kamera ilə çək" kataloqda).
11. Mətnlər qısaldıldı (UI daha yığcam).
12. Keramo Bazar loqosu: app ikonu (mipmap) + başlıqda kiçik nişan.
13. Ölçüyə uyğun çərçivə: 60×120, 60×30, 60×60, 50×50, 40×40, 60×20 (kafel/metlax),
    seçilən ölçünün nisbətinə görə çərçivə düzbucaqlı/kvadrat olur.
14. Çərçivə-çəkiliş tam uyğunlaşdırıldı (ekranda görünən = çəkilən, rotasiya fərqi düzəldildi).
15. Avtomatik yaxınlaşdırma (auto-zoom) — əl ilə sürüşdürmə yoxdur.
16. Şəkil ölçüsü 448→320px endirildi (tanıma dəqiqliyinə təsir etmədən, model
    hər halda 224px-ə endirir) — sürət/yaddaş üçün.
17. Böyüdülmüş baxış (lightbox): kataloq, axtarış nəticələri, dialoqlardakı şəkillərə basıb böyütmək.
18. 5-6 şəkil ardıcıl çəkmək (seriya rejimi, Kataloq), hər addım üçün bucaq/işıq təlimatı.
19. OCR ilə etiket oxuyub model adını avtomatik təklif etmək (redaktə edilə bilir).
20. Axtarışda "Növbəti şəkli çək" (ardıcıl axtarış).
21. Sabit debug keystore — APK yeniləndikdə artıq **uninstall tələb olunmur,
    kataloq itmir** (əvvəlki tək keçid istisna olmaqla).
22. Avtomatik versiyalama: hər "Run workflow" → v1, v2, v3… (APK adı və tətbiq
    daxilində versiya nömrəsi).

## Qurma təlimatı (hər dəfə)
1. GitHub-da dəyişən faylı aç → qələm (✏️) → köhnəni sil → yenisini yapışdır → Commit.
2. **Actions > Build APK > Run workflow**.
3. Bir neçə dəqiqəyə "plite-tap-vN" artifact-ı çıxır, APK-nı endir, telefonda aç.

## Bilinən məhdudiyyətlər
- OCR yalnız latın hərf/rəqəmi yaxşı oxuyur, Azərbaycan xüsusi hərflərini yox.
- Modellər WASM ilə işlədiyi üçün zəif telefonlarda kompüterdən yavaşdır.
- "Məsafə" (çərçivəni doldurma) yoxlaması təxmini bir heuristikdir, 100% deyil.
