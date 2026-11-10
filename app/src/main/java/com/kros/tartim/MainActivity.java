package com.kros.tartim;
import android.app.*;import android.os.*;import android.content.*;import android.net.Uri;import android.provider.MediaStore;import android.graphics.*;import android.graphics.drawable.GradientDrawable;import android.text.*;import android.view.*;import android.widget.*;
import com.google.mlkit.vision.common.InputImage;import com.google.mlkit.vision.text.Text;import com.google.mlkit.vision.text.TextRecognition;import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.InputStream;import java.util.*;import java.util.regex.*;
public class MainActivity extends Activity {
 static class Item {EditText name,rate,actual;TextView target;LinearLayout card;String original="";}
 ArrayList<Item> items=new ArrayList<>();LinearLayout root,rows;EditText batch;TextView status,summary;Uri photo;boolean approved=false,loading=false;int scanned=0;
 int dp(int x){return (int)(x*getResources().getDisplayMetrics().density);}
 GradientDrawable shape(int color){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(15));return g;}
 TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(2),dp(6),dp(2),dp(6));return t;}
 EditText field(String hint,String value){EditText e=new EditText(this);e.setSingleLine(true);e.setText(value);e.setHint(hint);e.setTextSize(15);e.setTextColor(0xFF10233C);return e;}
 void button(LinearLayout parent,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(-1);b.setBackground(shape(0xFF1463BA));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));lp.setMargins(0,dp(5),0,dp(5));parent.addView(b,lp);b.setOnClickListener(v->action.run());}
 void changed(){if(!loading){approved=false;refresh();}}
 TextWatcher watcher(){return new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){changed();}public void afterTextChanged(Editable e){}};}
 double number(String s){return Double.parseDouble(s.trim().replace(" ","").replace(",","."));}
 void splash(){LinearLayout v=new LinearLayout(this);v.setGravity(Gravity.CENTER);v.setOrientation(1);v.setBackgroundColor(0xFF071C36);ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.kros_logo);v.addView(logo,new LinearLayout.LayoutParams(dp(110),dp(110)));TextView t=text("POLİTEM",31,-1);t.setTypeface(null,1);v.addView(t);TextView sub=text("KROS • Akıllı Reçete ve Tartım",15,0xFFB6D9F8);v.addView(sub);setContentView(v);new Handler(Looper.getMainLooper()).postDelayed(this::mainScreen,900);}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xFF071C36);splash();}
 void mainScreen(){ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xFFF0F4FA);root=new LinearLayout(this);root.setPadding(dp(16),dp(16),dp(16),dp(24));root.setOrientation(1);scroll.addView(root);setContentView(scroll);
 LinearLayout hero=new LinearLayout(this);hero.setGravity(Gravity.CENTER_VERTICAL);hero.setPadding(dp(15),dp(12),dp(15),dp(12));hero.setBackground(shape(0xFF071C36));ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.kros_logo);hero.addView(logo,new LinearLayout.LayoutParams(dp(62),dp(62)));LinearLayout h=new LinearLayout(this);h.setPadding(dp(12),0,0,0);h.setOrientation(1);h.addView(text("POLİTEM  |  KROS",21,-1));h.addView(text("Dinamik reçete • Akıllı tartım",12,0xFFB6D9F8));hero.addView(h);root.addView(hero);
 button(root,"📷 İş emri fotoğrafı çek",this::camera);button(root,"🖼 Galeriden iş emri seç",this::gallery);
 status=text("Her ürün için farklı malzeme listesi oluşturabilirsiniz. OCR sonuçlarını doğrulayın.",13,0xFF475569);root.addView(status);
 root.addView(text("ÜRETİM MİKTARI (KG)",13,0xFF334155));batch=field("Örn. 6000","");batch.setInputType(8194);root.addView(batch);batch.addTextChangedListener(watcher());
 root.addView(text("MALZEME  /  ORAN  /  HEDEF  /  TARTILAN",13,0xFF334155));rows=new LinearLayout(this);rows.setOrientation(1);root.addView(rows);
 button(root,"+ Yeni hammadde ekle",()->addItem("","",""));
 button(root,"Hedefleri hesapla",()->{refresh();if(!valid(false))status.setText("Oranları ve üretim miktarını kontrol edin.");});
 button(root,"Reçeteyi doğruladım • Onayla",()->{if(valid(true)){approved=true;learnAliases();refresh();status.setText("Reçete onaylandı. Tartım değerleri ayrıca doğrulanmalıdır.");}});
 summary=text("",14,0xFF0F172A);root.addView(summary);
 button(root,"Onaylı raporu paylaş",()->{if(!approved||!valid(true)){Toast.makeText(this,"Önce reçeteyi doğrulayın",1).show();return;}Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,report());startActivity(Intent.createChooser(i,"Rapor"));});
 refresh();
 }
 void addItem(String name,String rate,String original){Item it=new Item();it.original=original;it.card=new LinearLayout(this);it.card.setOrientation(1);it.card.setPadding(dp(12),dp(9),dp(12),dp(10));it.card.setBackground(shape(-1));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(5),0,dp(6));rows.addView(it.card,cp);
 it.name=field("Hammadde adı",name);it.card.addView(it.name);
 LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);it.card.addView(line);
 it.rate=field("Oran %",rate);it.rate.setInputType(8194);line.addView(it.rate,new LinearLayout.LayoutParams(0,dp(54),1));
 it.target=text("— kg",14,0xFF047857);it.target.setGravity(Gravity.CENTER);it.target.setTypeface(null,1);line.addView(it.target,new LinearLayout.LayoutParams(0,dp(54),1.1f));
 it.actual=field("Tartılan kg","");it.actual.setInputType(8194);line.addView(it.actual,new LinearLayout.LayoutParams(0,dp(54),1.25f));
 TextView labels=text("ORAN %               HEDEF KG                TARTILAN KG",10,0xFF64748B);it.card.addView(labels);
 Button remove=new Button(this);remove.setText("Satırı kaldır");remove.setAllCaps(false);it.card.addView(remove);remove.setOnClickListener(v->{rows.removeView(it.card);items.remove(it);changed();});
 items.add(it);it.name.addTextChangedListener(watcher());it.rate.addTextChangedListener(watcher());it.actual.addTextChangedListener(watcher());refresh();}
 void refresh(){if(batch==null)return;double total=0;try{total=number(batch.getText().toString());}catch(Exception ignored){}
 double sum=0;int good=0;for(Item it:items){try{double p=number(it.rate.getText().toString());if(p<0||p>100||!Double.isFinite(p))throw new Exception();sum+=p;if(total>0&&Double.isFinite(total)){it.target.setText(String.format(Locale.forLanguageTag("tr-TR"),"%.3f kg",total*p/100));good++;}else it.target.setText("— kg");}catch(Exception e){it.target.setText("— kg");}}
 if(summary!=null)summary.setText("Malzeme: "+items.size()+"  |  Hesaplanan: "+good+"  |  Oran toplamı: "+String.format(Locale.forLanguageTag("tr-TR"),"%.2f%%",sum)+"\nReçete onayı: "+(approved?"EVET":"HAYIR")+". Tartım alanları gerçek ölçüm içindir.");}
 boolean valid(boolean notify){double total,sum=0;try{total=number(batch.getText().toString());if(!Double.isFinite(total)||total<=0||items.isEmpty())throw new Exception();for(Item it:items){if(it.name.getText().toString().trim().isEmpty())throw new Exception();double p=number(it.rate.getText().toString());if(!Double.isFinite(p)||p<0||p>100)throw new Exception();sum+=p;String a=it.actual.getText().toString().trim();if(!a.isEmpty()&&(!Double.isFinite(number(a))||number(a)<0))throw new Exception();}if(Math.abs(sum-100)>0.02)throw new Exception();return true;}catch(Exception e){if(notify)Toast.makeText(this,"Üretim kg, malzeme adları ve oran toplamı (%100) kontrol edilmeli.",1).show();return false;}}
 String report(){StringBuilder b=new StringBuilder("KROS | POLİTEM\nÜretim: "+batch.getText()+" kg\n");for(Item it:items)b.append(it.name.getText()).append(" | %").append(it.rate.getText()).append(" | hedef ").append(it.target.getText()).append(" | tartılan ").append(it.actual.getText()).append("\n");b.append("Reçete operatör onayı: ").append(approved?"EVET":"HAYIR");return b.toString();}
 void learnAliases(){android.content.SharedPreferences p=getSharedPreferences("kros_aliases",0);android.content.SharedPreferences.Editor e=p.edit();for(Item it:items){String raw=it.original.trim().toLowerCase(Locale.ROOT);String corrected=it.name.getText().toString().trim();if(raw.length()>3&&corrected.length()>2&&raw.length()<120&&corrected.length()<120)e.putString(raw,corrected);}e.apply();}
 String knownName(String raw){return getSharedPreferences("kros_aliases",0).getString(raw.trim().toLowerCase(Locale.ROOT),raw);}
 void camera(){try{android.content.ContentValues v=new android.content.ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"kros_"+System.currentTimeMillis()+".jpg");v.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");photo=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,photo);i.addFlags(3);startActivityForResult(i,1);}catch(Exception e){status.setText("Kamera hatası: "+e.getMessage());}}
 void gallery(){Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");startActivityForResult(i,2);}
 @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK)return;Uri uri=req==1?photo:data==null?null:data.getData();if(uri==null)return;approved=false;status.setText("AI OCR taraması: dört yönde analiz ediliyor...");try{InputStream in=getContentResolver().openInputStream(uri);BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeStream(in,null,options);in.close();int sample=1;while(Math.max(options.outWidth,options.outHeight)/sample>2200)sample*=2;options.inJustDecodeBounds=false;options.inSampleSize=sample;in=getContentResolver().openInputStream(uri);Bitmap bmp=BitmapFactory.decodeStream(in,null,options);in.close();if(bmp==null)throw new Exception("Görsel okunamadı");scan(bmp,0,null,-1);}catch(Exception e){status.setText("Görsel hatası: "+e.getMessage());}}
 void scan(Bitmap base,int rotation,Text best,int bestScore){if(rotation==4){base.recycle();if(best!=null)extract(best);else status.setText("OCR metni okunamadı");return;}Matrix m=new Matrix();m.postRotate(rotation*90);Bitmap rotated=rotation==0?base:Bitmap.createBitmap(base,0,0,base.getWidth(),base.getHeight(),m,true);TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(InputImage.fromBitmap(rotated,0)).addOnSuccessListener(t->{int score=t.getText().length()/40;for(Text.TextBlock b:t.getTextBlocks())for(Text.Line l:b.getLines()){String s=l.getText().toLowerCase(Locale.ROOT);if(s.contains("oran")||s.contains("malzeme")||s.contains("miktar"))score+=4;if(s.contains("%"))score+=2;}if(rotation!=0)rotated.recycle();scan(base,rotation+1,score>bestScore?t:best,Math.max(bestScore,score));}).addOnFailureListener(e->{if(rotation!=0)rotated.recycle();scan(base,rotation+1,best,bestScore);});}
 static class Cell{String s;Rect r;Cell(String s,Rect r){this.s=s;this.r=r;}}
 void extract(Text t){
 ArrayList<Cell> lines=new ArrayList<>(),elements=new ArrayList<>();
 for(Text.TextBlock b:t.getTextBlocks())for(Text.Line l:b.getLines()){
  if(l.getBoundingBox()!=null)lines.add(new Cell(l.getText().trim(),l.getBoundingBox()));
  for(Text.Element e:l.getElements())if(e.getBoundingBox()!=null)elements.add(new Cell(e.getText().trim(),e.getBoundingBox()));
 }
 Collections.sort(lines,(a,b)->Integer.compare(a.r.centerY(),b.r.centerY()));
 String full=t.getText();getSharedPreferences("kros_diagnostics",0).edit().putString("last_ocr",full).apply();
 int column=-1,headerY=-1;
 for(Cell e:elements){String s=e.s.toLowerCase(Locale.ROOT).replace('ı','i');if(s.contains("oran")&&e.r.centerY()<Math.max(400,lines.isEmpty()?400:lines.get(lines.size()-1).r.bottom)){column=e.r.centerX();headerY=e.r.centerY();break;}}
 Pattern numeric=Pattern.compile("^[% ]*([0-9]{1,2}(?:[,.][0-9]{1,3})?)[% ]*$");
 ArrayList<Cell> values=new ArrayList<>();
 for(Cell e:elements){String raw=e.s.trim();Matcher m=numeric.matcher(raw);if(!m.matches())continue;try{double v=number(m.group(1));if(v>=0&&v<=100&&(headerY<0||e.r.centerY()>headerY))values.add(new Cell(m.group(1),e.r));}catch(Exception ignored){}}
 // Only use a percentage column when a reliable header or explicit percent signs identify it.
 if(column<0){ArrayList<Cell> marked=new ArrayList<>();for(Cell e:elements)if(e.s.contains("%"))marked.add(e);
  if(marked.size()>=2){int sum=0;for(Cell e:marked)sum+=e.r.centerX();column=sum/marked.size();}
 }
 ArrayList<String[]> candidates=new ArrayList<>();HashSet<Integer> used=new HashSet<>();
 if(column>=0){
  for(Cell v:values){
   if(Math.abs(v.r.centerX()-column)>Math.max(65,v.r.height()*5))continue;
   Cell best=null;int score=Integer.MAX_VALUE;
   for(Cell n:lines){
    if(n.r.centerX()>=v.r.centerX()||n.r.centerY()<=headerY)continue;
    String s=n.s.toLowerCase(Locale.ROOT);
    if(s.contains("toplam")||s.contains("oran")||s.contains("miktar")||s.contains("malzeme")||!n.s.matches(".*[A-Za-zÇĞİÖŞÜçğıöşü].*"))continue;
    int dy=Math.abs(n.r.centerY()-v.r.centerY());
    if(dy>Math.max(38,n.r.height()*3))continue;
    int dx=Math.abs(n.r.right-v.r.left);
    int candidateScore=dy*4+dx/10;
    if(candidateScore<score){best=n;score=candidateScore;}
   }
   if(best==null||used.contains(best.r.centerY()))continue;
   String name=best.s.replaceFirst("^\\s*\\d{1,2}[.)-]\\s*","").trim();
   if(name.length()<4)continue;
   candidates.add(new String[]{name,v.s});used.add(best.r.centerY());
  }
 }
 if(!candidates.isEmpty()){
  loading=true;items.clear();rows.removeAllViews();
  for(String[] entry:candidates)addItem(knownName(entry[0]),entry[1],entry[0]);
  loading=false;
 }
 approved=false;refresh();
 double sum=0;for(String[] entry:candidates)try{sum+=number(entry[1]);}catch(Exception ignored){}
 status.setText("OCR: "+lines.size()+" satır, "+values.size()+" sayı, "+candidates.size()+" malzeme adayı. Oran toplamı: "+String.format(Locale.forLanguageTag("tr-TR"),"%.2f",sum)+"%. "+(column<0?"Oran sütunu güvenle belirlenemedi. ":"")+"Tüm değerleri belgeyle doğrulayın.");
 if(candidates.isEmpty()){
  new AlertDialog.Builder(this).setTitle("OCR tanılama").setMessage("Metin algılandı ancak güvenilir malzeme-oran eşleşmesi yapılamadı.\n\n"+(full.length()>1800?full.substring(0,1800):full)).setPositiveButton("Tamam",null).show();
 }
 }
}
}