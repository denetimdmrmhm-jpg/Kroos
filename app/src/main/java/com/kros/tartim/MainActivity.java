package com.kros.tartim;
import android.app.*;import android.os.*;import android.content.*;import android.net.Uri;import android.provider.MediaStore;import android.text.InputType;import android.text.TextWatcher;import android.text.Editable;import android.view.*;import android.widget.*;
import com.google.mlkit.vision.common.InputImage;import com.google.mlkit.vision.text.Text;import android.graphics.Rect;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.graphics.Matrix;import java.io.InputStream;import com.google.mlkit.vision.text.TextRecognition;import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.util.*;import java.util.regex.*;
public class MainActivity extends Activity {
 final String[] names={"PA6 Natural RV 2.51-2.80","13 Micron Glass Fiber","CA-ST Granul","Phenolic Antioxidant","Mould Release Agent","Richfos 168G / Irgafos 168FF","PA6 Natural RV 3.21-3.60"};
 final double[] preset={34.6,30,0.3,0.1,0.2,0.2,34.6};EditText[] rates=new EditText[7],actual=new EditText[7];EditText batch,order;TextView status,summary;LinearLayout root;Uri photo;boolean approved=false;boolean updating=false;
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}TextView label(String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(16);t.setPadding(0,dp(8),0,dp(4));return t;}
 EditText num(String s){EditText e=new EditText(this);e.setSingleLine(true);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);e.setText(s);return e;}
 void button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->r.run());root.addView(b);}
 public void onCreate(Bundle b){super.onCreate(b);ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(16),dp(12),dp(16),dp(20));sc.addView(root);setContentView(sc);
 root.addView(label("KROS | Akilli Tartim"));button("Is emrinin fotografini cek",this::camera);button("Galeriden is emri sec",this::gallery);
 status=label("Is emri fotografini secin. OCR sonrasinda tum alanlari elle dogrulayin.");root.addView(status);root.addView(label("Is emri no"));order=new EditText(this);order.setSingleLine(true);order.setText("");root.addView(order);
 root.addView(label("Uretim miktari (kg)"));batch=num("");root.addView(batch);
 for(int i=0;i<7;i++){root.addView(label((i+1)+". "+names[i]));LinearLayout line=new LinearLayout(this);line.setOrientation(0);rates[i]=num("");rates[i].setHint("Oran %");actual[i]=num("");actual[i].setHint("Tartilan kg");line.addView(rates[i],new LinearLayout.LayoutParams(0,dp(55),1));line.addView(actual[i],new LinearLayout.LayoutParams(0,dp(55),1));root.addView(line);}
 TextWatcher invalidate=new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){if(!updating){approved=false;if(status!=null)status.setText("Recete veya miktar degisti; yeniden operator onayi gerekli.");}}public void afterTextChanged(Editable e){}};batch.addTextChangedListener(invalidate);order.addTextChangedListener(invalidate);for(EditText e:rates)e.addTextChangedListener(invalidate);for(EditText e:actual)e.addTextChangedListener(invalidate);
 button("Hedefleri hesapla",()->{approved=false;calculate();});button("Receteyi kontrol ettim, onayliyorum",()->{if(validate()){approved=true;calculate();status.setText("Operator onayi alindi.");}});
 summary=label("");root.addView(summary);button("Raporu paylas",()->{if(!approved){toast("Once receteyi dogrulayin");return;}Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,summary.getText().toString());startActivity(Intent.createChooser(i,"Rapor"));});summary.setText("Fotograf secin veya is emri bilgilerini girin. Onay verilmeden rapor paylasilamaz.");}
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}double parse(String s){return Double.parseDouble(s.trim().replace(" ","").replace(",","."));}
 boolean validate(){try{double sum=0;for(EditText e:rates){double v=parse(e.getText().toString());if(!Double.isFinite(v)||v<0||v>100)throw new Exception();sum+=v;}if(Math.abs(sum-100)>0.02){toast("Recete toplami %100 degil: "+sum);return false;}double amount=parse(batch.getText().toString());if(!Double.isFinite(amount)||amount<=0){toast("Uretim miktarini kontrol edin");return false;}for(EditText e:actual){String entered=e.getText().toString().trim();if(!entered.isEmpty()){double measured=parse(entered);if(!Double.isFinite(measured)||measured<0){toast("Tartilan kg degerlerini kontrol edin");return false;}}}return true;}catch(Exception e){toast("Oranlari ve miktari kontrol edin");return false;}}
 void calculate(){if(!validate())return;double b=parse(batch.getText().toString());StringBuilder out=new StringBuilder("Is emri: "+order.getText()+"\nUretim: "+b+" kg\n");int ok=0;for(int j=0;j<7;j++){double target=b*parse(rates[j].getText().toString())/100;out.append(names[j]).append(" | hedef ").append(String.format(Locale.US,"%.3f",target)).append(" kg");try{String a=actual[j].getText().toString();if(!a.isEmpty()){double diff=parse(a)-target;out.append(" | fark ").append(String.format(Locale.US,"%+.3f",diff));if(Math.abs(diff)<=0.05)ok++;}}catch(Exception ignored){}out.append("\n");}out.append("Ornek tolerans +-0.05 kg, uygun: ").append(ok).append("/7\nOperator onayi: ").append(approved?"EVET":"HAYIR");summary.setText(out.toString());}
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
