package uchan.weather;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static java.lang.System.arraycopy;

public class Splashscreen extends Activity {

    Document doc = null;
    String res[] = new String[60];
    static final int LENGTH = 30;
    int i = 0;
    int numLength;
    int minusCnt =0;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    /**
     * Called when the activity is first created.
     */
    Thread splashTread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

//        GetXMLTask task = new GetXMLTask();
//        task.execute("http://openapi.seoul.go.kr:8088/6469596b45686f733535474c4c6672/xml/ListGoodFoodService/1/" + LENGTH + "/");

        StartAnimations();
    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout l = (LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView iv = (ImageView) findViewById(R.id.splash);
        iv.clearAnimation();
        iv.startAnimation(anim);

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 3500) {
                        sleep(100);
                        waited += 100;
                    }
                    Intent intent = new Intent(Splashscreen.this, MainActivity.class);
                    startActivity(intent);
                    Splashscreen.this.finish();
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    Splashscreen.this.finish();
                }

            }
        };
        splashTread.start();

    }

    //private inner class extending AsyncTask
    private class GetXMLTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... urls) {
//            URL url;
//            try {
//                url = new URL(urls[0]);
//                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//                DocumentBuilder db = dbf.newDocumentBuilder(); //XML문서 빌더 객체를 생성
//                doc = db.parse(new InputSource(url.openStream())); //XML문서를 파싱한다.
//                doc.getDocumentElement().normalize();
//
//            } catch (Exception e) {
//                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
//            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {
//            String[] s = null;
//            s = new String[LENGTH];
//            //row태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
//            NodeList nodeList = doc.getElementsByTagName("row");
//            //row 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환
//
//            for (i = 0; i < LENGTH; i++) {
//                //음식점 데이터 노드들을 하나씩 출력하기
//
//                Node node = nodeList.item(i); //row엘리먼트 노드
//                Element fstElmnt = (Element) node;
//
//                //CTF_TYPE_NAME
//                NodeList CTF_TYPE_NAME = fstElmnt.getElementsByTagName("CTF_TYPE_NAME");
//                Element nameElement = (Element) CTF_TYPE_NAME.item(0);
//                CTF_TYPE_NAME = nameElement.getChildNodes();
//                s[i] += ((Node) CTF_TYPE_NAME.item(0)).getNodeValue() + ";";
//                s[i] = s[i].substring(4);
//
//                NodeList CTF_NAME = fstElmnt.getElementsByTagName("CTF_NAME");
//                s[i] += CTF_NAME.item(0).getChildNodes().item(0).getNodeValue() + ";";
//
//                //x좌표
//                NodeList gradeList = fstElmnt.getElementsByTagName("CTF_X");
//                s[i] += gradeList.item(0).getChildNodes().item(0).getNodeValue() + ";";
//                // Log.d("X좌표",s[i]);
//
//                //y좌표
//                NodeList CTF_Y = fstElmnt.getElementsByTagName("CTF_Y");
//                s[i] += CTF_Y.item(0).getChildNodes().item(0).getNodeValue() + ";";
//                //  Log.d("s0",s[i]);
//
//                //OZONE 오존
//                NodeList CTF_ADDR = fstElmnt.getElementsByTagName("CTF_ADDR");
//                if (CTF_ADDR.item(0).getChildNodes().item(0).getNodeValue().toString().equals(" ")) {
//                    s[i] += "주소가 없습니다;";
//                } else {
//                    s[i] += CTF_ADDR.item(0).getChildNodes().item(0).getNodeValue() + ";";
//                }
//                try{
//                    NodeList CTF_TEL = fstElmnt.getElementsByTagName("CTF_TEL");
//                    s[i] += CTF_TEL.item(0).getChildNodes().item(0).getNodeValue();
//
//
//                }catch(Exception e){
//                    s[i] += " ";
//                    minusCnt++;
//                    continue;
//                }
//                Log.d("유짠" + String.valueOf(i), s[i]);
//                //음식점이름;X좌표;Y좌표;음식점주소
//                //list.add(s[i]);
//            }
//            numLength = i;
//            arraycopy(s, 0, res, 0, s.length);
        }
    }//end inner class - GetXMLTask
}