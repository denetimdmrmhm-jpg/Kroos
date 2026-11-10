# KROS Akıllı Tartım (Android)

Android kamera/galeri üzerinden iş emri fotoğrafını cihaz üzerinde ML Kit OCR ile okuyan **prototip**.

## Durum
- Android kaynak kodu ve GitHub Actions APK derleme iş akışı eklendi.
- OCR metin algılama mevcut; **genel amaçlı güvenilir reçete ayrıştırma henüz yoktur**.
- OCR yalnızca belirli katkı satırlarını örnek reçeteyle karşılaştırır; tüm satırlar okunmadığında örnek oranlar ekranda kalabilir.
- İş emri miktarı fotoğraftan otomatik alınmaz. Üretim miktarı elle girilir.
- Gerçek tartım verileri elle girilir; fiziksel kantara veya üretim makinesine bağlanmaz.
- Hedef değerler örnektir; operatörün fotoğraftaki her satırı kontrol etmesi gerekir.
- ±0,05 kg örnek toleranstır; yetkili proses mühendisinin onaylı toleransı değildir.
- Bu prototipi tek başına gerçek üretim dozaj kararı için kullanmayın.

## APK
GitHub > Actions > KROS Camera OCR APK > başarılı çalışma > Artifacts > KROS-Kamera-OCR-APK.
Workflow başarılı olursa debug APK üretir. Debug APK dağıtım için imzalı yayın sürümü değildir.

## Sonraki geliştirmeler
1. OCR sonrası tüm reçete satırlarını güvenli yapılandırılmış tabloya dönüştürme.
2. OCR güven skoru / okunamayan hücre işaretleme.
3. İş emri fotoğrafı ve manuel doğrulama ekranı yan yana.
4. Gerçek tartım ve onay için izlenebilir denetim kaydı.
5. Fiziksel cihaz bağlantıları ancak yetkili tesis ekibi gözetiminde.
