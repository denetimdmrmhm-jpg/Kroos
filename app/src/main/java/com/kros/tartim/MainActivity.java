package com.kros.tartim;
import android.app.*;import android.os.*;import android.content.*;import android.net.Uri;import android.provider.MediaStore;import android.text.InputType;import android.text.TextWatcher;import android.text.Editable;import android.view.*;import android.widget.*;
import com.google.mlkit.vision.common.InputImage;import com.google.mlkit.vision.text.Text;import android.graphics.Rect;import android.graphics.Color;import android.graphics.Typeface;import android.graphics.drawable.GradientDrawable;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.graphics.Matrix;import java.io.InputStream;import com.google.mlkit.vision.text.TextRecognition;import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.util.*;import java.util.regex.*;
public class MainActivity extends Activity {
 final String[] names={"PA6 Natural RV 2.51-2.80","13 Micron Glass Fiber","CA-ST Granul","Phenolic Antioxidant","Mould Release Agent","Richfos 168G / Irgafos 168FF","PA6 Natural RV 3.21-3.60"};
 final double[] preset={34.6,30,0.3,0.1,0.2,0.2,34.6};EditText[] rates=new EditText[7],actual=new EditText[7];TextView[] targets=new TextView[7];EditText batch,order;TextView status,summary;LinearLayout root;Uri photo;boolean approved=false;boolean updating=false;
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}TextView label(String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(16);t.setPadding(0,dp(8),0,dp(4));return t;}
 EditText num(String s){EditText e=new EditText(this);e.setSingleLine(true);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);e.setText(s);return e;}
 GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 void button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setTypeface(null,Typeface.BOLD);b.setBackground(bg(0xFF2563EB,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,dp(5),0,dp(7));root.addView(b,lp);b.setOnClickListener(v->r.run());}
 TextView targetLabel(String value){TextView t=label(value);t.setTextColor(0xFF0F766E);t.setTypeface(null,Typeface.BOLD);t.setTextSize(15);return t;}
 void updateTargets(){
  try{double total=parse(batch.getText().toString());if(!Double.isFinite(total)||total<=0)throw new Exception();
   for(int i=0;i<7;i++){try{double percent=parse(rates[i].getText().toString());if(!Double.isFinite(percent)||percent<0||percent>100)throw new Exception();targets[i].setText(String.format(Locale.forLanguageTag("tr-TR"),"%.3f kg",total*percent/100));}catch(Exception e){targets[i].setText("—");}}
  }catch(Exception e){for(TextView t:targets)if(t!=null)t.setText("—");}
 }
 public void onCreate(Bundle b){super.onCreate(b);
  getWindow().setStatusBarColor(0xFF0B1D39);getWindow().setNavigationBarColor(0xFF0B1D39);
  ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(0xFFF2F5FA);
  root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(18),dp(18),dp(30));sc.addView(root);setContentView(sc);
  LinearLayout header=new LinearLayout(this);header.setOrientation(0);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(16),dp(15),dp(16),dp(15));header.setBackground(bg(0xFF0B1D39,18));
  ImageView logo=new ImageView(this);logo.setImageResource(com.kros.tartim.R.drawable.kros_logo);header.addView(logo,new LinearLayout.LayoutParams(dp(58),dp(58)));
  LinearLayout titleBox=new LinearLayout(this);titleBox.setOrientation(1);titleBox.setPadding(dp(12),0,0,0);
  TextView title=label("KROS");title.setTextSize(25);title.setTypeface(null,Typeface.BOLD);title.setTextColor(Color.WHITE);titleBox.addView(title);
  TextView sub=label("Akıllı Reçete & Tartım");sub.setTextSize(13);sub.setTextColor(0xFFBFD5F4);titleBox.addView(sub);header.addView(titleBox);root.addView(header);
  root.addView(label("İŞ EMRİ OKUMA"));button("Fotoğraf çek • Akıllı OCR",this::camera);button("Galeriden iş emri seç",this::gallery);
  status=label("İş emrini seçin. OCR sonuçları kontrol edilmelidir.");root.addView(status);
  root.addView(label("İş emri numarası (isteğe bağlı)"));order=new EditText(this);order.setSingleLine(true);root.addView(order);
  root.addView(label("Üretim miktarı (kg)"));batch=num("");root.addView(batch);
  root.addView(label("REÇETE • HEDEF • TARTIM"));
  for(int i=0;i<7;i++){
   LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(12),dp(10),dp(12),dp(12));card.setBackground(bg(Color.WHITE,14));
   LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(5),0,dp(6));root.addView(card,cp);
   TextView name=label((i+1)+". "+names[i]);name.setTextColor(0xFF182B48);name.setTypeface(null,Typeface.BOLD);card.addView(name);
   LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);line.setOrientation(0);
   rates[i]=num("");rates[i].setHint("Oran %");line.addView(rates[i],new LinearLayout.LayoutParams(0,dp(56),1));
   targets[i]=targetLabel("—");targets[i].setGravity(Gravity.CENTER);line.addView(targets[i],new LinearLayout.LayoutParams(0,dp(56),1.25f));
   actual[i]=num("");actual[i].setHint("Tartılan kg");line.addView(actual[i],new LinearLayout.LayoutParams(0,dp(56),1.4f));card.addView(line);
   TextView columns=label("Oran %                     Hedef kg                  Tartılan kg");columns.setTextSize(11);columns.setTextColor(0xFF64748B);card.addView(columns);
  }
  TextWatcher watcher=new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){if(!updating){approved=false;updateTargets();if(status!=null)status.setText("Değerler değişti; reçeteyi yeniden doğrulayın.");}}public void afterTextChanged(Editable e){}};
  batch.addTextChangedListener(watcher);order.addTextChangedListener(watcher);for(EditText e:rates)e.addTextChangedListener(watcher);for(EditText e:actual)e.addTextChangedListener(watcher);
  button("Hedefleri hesapla",()->{approved=false;calculate();});
  button("Reçeteyi kontrol ettim, onaylıyorum",()->{if(validate()){approved=true;calculate();status.setText("Reçete onaylandı. Tartım değerleri ayrıca kontrol edilmelidir.");}});
  summary=label("Hedef kg değerleri her malzemenin yanında gösterilir. Tartılan kg alanına gerçek ölçümü girin.");root.addView(summary);
  button("Raporu paylaş",()->{if(!approved){toast("Önce reçeteyi doğrulayın");return;}Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,summary.getText().toString());startActivity(Intent.createChooser(i,"Rapor"));});
 }
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}double parse(String s){return Double.parseDouble(s.trim().replace(" ","").replace(",","."));}
 boolean validate(){try{double sum=0;for(EditText e:rates){double v=parse(e.getText().toString());if(!Double.isFinite(v)||v<0||v>100)throw new Exception();sum+=v;}if(Math.abs(sum-100)>0.02){toast("Recete toplami %100 degil: "+sum);return false;}double amount=parse(batch.getText().toString());if(!Double.isFinite(amount)||amount<=0){toast("Uretim miktarini kontrol edin");return false;}for(EditText e:actual){String entered=e.getText().toString().trim();if(!entered.isEmpty()){double measured=parse(entered);if(!Double.isFinite(measured)||measured<0){toast("Tartilan kg degerlerini kontrol edin");return false;}}}return true;}catch(Exception e){toast("Oranlari ve miktari kontrol edin");return false;}}
 void calculate(){if(!validate())return;double b=parse(batch.getText().toString());StringBuilder out=new StringBuilder("Is emri: "+order.getText()+"\nUretim: "+b+" kg\n");int ok=0;for(int j=0;j<7;j++){double target=b*parse(rates[j].getText().toString())/100;out.append(names[j]).append(" | hedef ").append(String.format(Locale.US,"%.3f",target)).append(" kg");try{String a=actual[j].getText().toString();if(!a.isEmpty()){double diff=parse(a)-target;out.append(" | fark ").append(String.format(Locale.US,"%+.3f",diff));if(Math.abs(diff)<=0.05)ok++;}}catch(Exception ignored){}out.append("\n");}out.append("Ornek tolerans +-0.05 kg, uygun: ").append(ok).append("/7\nOperator onayi: ").append(approved?"EVET":"HAYIR");summary.setText("Üretim: "+String.format(Locale.forLanguageTag("tr-TR"),"%.1f",b)+" kg • Reçete onayı: "+(approved?"EVET":"HAYIR")+"\nTartım uygunluğu: "+ok+"/7 (ölçülmemiş satırlar hariç).\n"+out.toString());updateTargets();}
 void camera(){try{android.content.ContentValues values=new android.content.ContentValues();values.put(MediaStore.Images.Media.DISPLAY_NAME,"kroos_"+System.currentTimeMillis()+".jpg");values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");photo=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,photo);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);startActivityForResult(i,1);}catch(Exception e){toast("Kamera hatasi: "+e.getMessage());}}
 void gallery(){Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");startActivityForResult(i,2);}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK)return;Uri uri=request==1?photo:(data==null?null:data.getData());if(uri==null){toast("Fotograf alinamadi");return;}approved=false;for(EditText rate:rates)rate.setText("");status.setText("Fotograf okunuyor... Tum oranlar yeniden dogrulanmali.");try{
  InputStream stream=getContentResolver().openInputStream(uri);
  Bitmap original=BitmapFactory.decodeStream(stream);
  stream.close();
  if(original==null)throw new IllegalArgumentException("Gorsel okunamadi");
  // Downsample very large images before rotating to reduce memory pressure.
  int max=Math.max(original.getWidth(),original.getHeight());
  if(max>2400){float scale=2400f/max;Bitmap small=Bitmap.createScaledBitmap(original,Math.max(1,Math.round(original.getWidth()*scale)),Math.max(1,Math.round(original.getHeight()*scale)),true);original.recycle();original=small;}
  scanOrientation(original,0,null,0);
 }catch(Exception e){status.setText("Fotograf acilamadi: "+e.getMessage());}}
 void scanOrientation(Bitmap source,int turn,Text best,int bestScore){
  if(turn==4){
   if(best!=null)extractTable(best);
   else status.setText("OCR metni okunamadi. Daha net fotograf deneyin.");
   source.recycle();return;
  }
  Matrix matrix=new Matrix();matrix.postRotate(turn*90);
  Bitmap rotated=turn==0?source:Bitmap.createBitmap(source,0,0,source.getWidth(),source.getHeight(),matrix,true);
  TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(InputImage.fromBitmap(rotated,0))
   .addOnSuccessListener(result->{
    int score=0;String normalized=result.getText().toLowerCase(Locale.ROOT).replace('ı','i');
    for(String line:normalized.split("\\n"))if(materialIndex(line)>=0)score+=5;
    if(normalized.contains("malzeme"))score+=3;
    if(normalized.contains("oran"))score+=3;
    if(normalized.contains("miktar"))score+=2;
    if(turn!=0)rotated.recycle();
    scanOrientation(source,turn+1,score>bestScore?result:best,Math.max(score,bestScore));
   }).addOnFailureListener(e->{
    if(turn!=0)rotated.recycle();
    scanOrientation(source,turn+1,best,bestScore);
   });
 }

 int materialIndex(String s){
  String q=s.toLowerCase(Locale.ROOT).replace('ı','i');
  if(q.contains("2.51")||q.contains("2,51"))return 0;
  if(q.contains("micron")||q.contains("glass fiber"))return 1;
  if(q.contains("ca-st")||q.contains("ca st"))return 2;
  if(q.contains("phenolic")||q.contains("antioxidant"))return 3;
  if(q.contains("mould release"))return 4;
  if(q.contains("richfos")||q.contains("irgafos"))return 5;
  if(q.contains("3.21")||q.contains("3,21"))return 6;
  return -1;
 }
 static class OcrCell {String s;Rect r;OcrCell(String s,Rect r){this.s=s;this.r=r;}}
 void extractTable(Text result){
  ArrayList<OcrCell> cells=new ArrayList<>();
  for(Text.TextBlock b:result.getTextBlocks())for(Text.Line line:b.getLines())
   if(line.getBoundingBox()!=null)cells.add(new OcrCell(line.getText(),line.getBoundingBox()));
  String full=result.getText();
  Matcher id=Pattern.compile("(?i)(?:is|iş)\\s*emri\\s*no\\s*[:\\-]?\\s*([0-9]+(?:[-/][0-9]+)*)").matcher(full);
  if(id.find())order.setText(id.group(1));
  // Detect the percentage column geometrically, never confuse it with quantity or lot numbers.
  int percentX=-1;int headerY=-1;
  for(OcrCell cell:cells){
   String s=cell.s.toLowerCase(Locale.ROOT).replace('ı','i');
   if(s.contains("malzeme orani")||s.contains("malzeme oranı")){
    percentX=cell.r.centerX();headerY=cell.r.centerY();break;
   }
  }
  int found=0;
  if(percentX>=0){
   ArrayList<OcrCell> rowNames=new ArrayList<>();
   for(OcrCell cell:cells)if(materialIndex(cell.s)>=0&&cell.r.centerY()>headerY)rowNames.add(cell);
   // Rows may wrap; choose the nearest percentage cell to each material row.
   boolean[] used=new boolean[7];
   for(OcrCell name:rowNames){
    int ix=materialIndex(name.s);if(ix<0||used[ix])continue;
    OcrCell best=null;double bestScore=Double.MAX_VALUE;
    for(OcrCell candidate:cells){
     if(candidate.r.centerY()<=headerY||candidate.r.centerX()<=name.r.centerX())continue;
     String raw=candidate.s.trim().replace("%","").replace(" ","");
     if(!raw.matches("[0-9]{1,2}[,.][0-9]{1,4}"))continue;
     double value;try{value=parse(raw);}catch(Exception e){continue;}
     if(value<0||value>100)continue;
     int dx=Math.abs(candidate.r.centerX()-percentX);
     int dy=Math.abs(candidate.r.centerY()-name.r.centerY());
     int height=Math.max(name.r.height(),candidate.r.height());
     if(dx>Math.max(55,height*4)||dy>height*2)continue;
     double score=dx+dy*2.0;
     if(score<bestScore){best=candidate;bestScore=score;}
    }
    if(best!=null){rates[ix].setText(String.valueOf(parse(best.s.replace("%",""))));used[ix]=true;found++;}
   }
  }
  approved=false;
  summary.setText("OCR sonucu taslaktir. Is emri, miktar ve oranlari orijinal belge ile dogrulayin.");
  status.setText("OCR: "+found+"/7 oran konumsal eslestirildi. "+(percentX<0?"Oran sutunu bulunamadi. ":"")+"Belirsiz alanlar bos birakildi; operator onayi zorunlu.");
 }
}
