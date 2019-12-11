package uchan.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.github.clans.fab.FloatingActionMenu;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;

import kr.go.seoul.culturalevents.CulturalEventButtonTypeA;
import kr.go.seoul.culturalevents.CulturalEventButtonTypeB;
import kr.go.seoul.culturalevents.CulturalEventTypeMini;
import uchan.weather.login.LoginRequest;
import uchan.weather.login.ModifyActivity;
import uchan.weather.login.RegisterActivity;
import uchan.weather.mail.MailActivity;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class MainActivity extends AppCompatActivity {

    final static private String KEY = "4b4d6443477379633231686d535077";
    PieChart pieChart;
    Document doc = null;
    final Context context = this;
    final String[] items = {"중구", "종로구", "용산구", "은평구"
            , "서대문구", "마포구", "광진구", "성동구", "중랑구", "동대문구"
            , "성북구", "도봉구", "강북구", "노원구", "강서구", "구로구", "영등포구"
            , "동작구", "관악구", "금천구", "양천구", "강남구", "서초구", "송파구", "강동구"};
    String[] element = {"Default", "Default", "Default", "Default", "Default", "Default", "Default", "Default", "Default"};
    private long back_time = 0;

    /* 회원정보*/
    private String userID = null;
    private String userPassword = null;
    private String userName = null;
    private int userAge = 0;
    int autoCheck = 0;
    int localnm = 21; // 지역 index

    /* Floating Button */
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;

    /* 문화정보 */
    private String key = "4c5057687673796338397974505555";
    private CulturalEventTypeMini typeMini;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Title bar setting */
        getSupportActionBar().setElevation(30); // 그림자(높낮이효과)
        getSupportActionBar().setSubtitle("대기정보와 문화정보"); // 서브타이틀
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.hamburger_btn);

        /* Preference 불러오기 */
        SharedPreferences setting_pre = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences auto_privacy = getSharedPreferences("auto_privacy", MODE_PRIVATE);
        autoCheck = auto_privacy.getInt("autoCheck", 0);
        localnm = getArrayIndex(R.array.array_local_values, setting_pre.getString("localnm", "21")); // Default : 21(강남구)

        /* Init */
        final Button btn_login = (Button) findViewById(R.id.btn_login);
        final Button btn_logout = (Button) findViewById(R.id.btn_logout);
        final TextView modify_member = (TextView) findViewById(R.id.modify_member);
        final TextView mail = (TextView) findViewById(R.id.email);
        final TextView share = (TextView) findViewById(R.id.share);
        final TextView standard = (TextView) findViewById(R.id.standard);
        final TextView setting = (TextView) findViewById(R.id.setting);

        Intent intent = getIntent();
        /* True : 자동 로그인(info에서 저장된 값 불러오기)*/
        if (autoCheck > 0) {
            userID = auto_privacy.getString("userID", "");
            userPassword = auto_privacy.getString("userPassword", "");
            userName = auto_privacy.getString("userName", "");
            userAge = auto_privacy.getInt("userAge", 0);
        } else {
            /* False : 일반 로그인 (Intent로 값 전달받기) */
            userID = intent.getStringExtra("userID");
            userPassword = intent.getStringExtra("userPassword");
            userName = intent.getStringExtra("userName");
            userAge = intent.getIntExtra("userAge", 1);
        }

        /* 로그인전/후 UI 변경 */
        if (userID == null) {
            btn_login.setVisibility(View.VISIBLE);
            btn_logout.setVisibility(View.GONE);
            modify_member.setText("Guest");
        } else {
            btn_login.setVisibility(View.GONE);
            btn_logout.setVisibility(View.VISIBLE);
            /* 닉네임 사용하는지 아이디 사용하는지 체크*/
            if (setting_pre.getBoolean("useUserNickname", false))
                modify_member.setText(setting_pre.getString("nickname", "nickname"));
            else
                modify_member.setText(userID);
        }

        /* XML 파싱 */
        new GetXMLTask().execute();

        final Button lc_list = (Button) findViewById(R.id.lc_list);
        final Button detail = (Button) findViewById(R.id.detail);
        lc_list.setText(items[localnm] + "  "); // Default : 강남구

//        /* Tab Layout Setting */
//        TabHost tabHost1 = (TabHost) findViewById(R.id.tabHost1);
//        tabHost1.setup();
//
//        // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
//        TabHost.TabSpec ts1 = tabHost1.newTabSpec("Tab Spec 1");
//        ts1.setContent(R.id.content1);
//        ts1.setIndicator("대기정보");
//        tabHost1.addTab(ts1);
//
//        // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
//        TabHost.TabSpec ts2 = tabHost1.newTabSpec("Tab Spec 2");
//        ts2.setContent(R.id.content2);
//        ts2.setIndicator("뭐하지");
//        tabHost1.addTab(ts2);

//        세 번째 Tab. (탭 표시 텍스트:"TAB 3"), (페이지 뷰:"content3")
//        TabHost.TabSpec ts3 = tabHost1.newTabSpec("Tab Spec 3");
//        ts3.setContent(R.id.content3);
//        ts3.setIndicator("기타");
//        tabHost1.addTab(ts3);

        /* Floating Button Setting */
        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);

        /* 오전 7시 알림 */
        if (setting_pre.getBoolean("alarm", false)) Alarm();
        else Alarm_cancel();

        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.lc_list:
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                        alertDialogBuilder.setTitle("설정에서 즐겨찾기 등록가능해요!");
                        alertDialogBuilder.setSingleChoiceItems(items, 0,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        localnm = which;
                                    }
                                })
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        lc_list.setText(items[localnm] + "  "); // 버튼이름 변경
                                        Toast.makeText(getApplicationContext(),
                                                items[localnm] + "를 선택했습니다.",
                                                Toast.LENGTH_SHORT).show();
                                        new GetXMLTask().execute(); // xml 불러오기
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        // 다이얼로그 생성
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        // 다이얼로그 보여주기
                        alertDialog.show();
                        break;
                    case R.id.detail:
                        if (userID == null) {
                            AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(context);
                            alertDialogBuilder2.setTitle(items[localnm] + " 기상 상세");
                            alertDialogBuilder2.setMessage("로그인 후 이용 가능한 서비스입니다.\n지금 로그인하시겠습니까?");
                            alertDialogBuilder2.setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    show_login();
                                }
                            });
                            alertDialogBuilder2.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //todo
                                }
                            });
                            // 다이얼로그 생성
                            AlertDialog alertDialog2 = alertDialogBuilder2.create();
                            // 다이얼로그 보여주기
                            alertDialog2.show();
                        } else show_detail();
                        break;
                    case R.id.material_design_floating_action_menu_item1:
                        Toast.makeText(getApplicationContext(), "Button1 Click", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.material_design_floating_action_menu_item2:
                        Toast.makeText(getApplicationContext(), "Button2 Click", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.material_design_floating_action_menu_item3:
                        Toast.makeText(getApplicationContext(), "Button3 Click", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.btn_login:
                        show_login();
                        break;
                    case R.id.btn_logout: // 로그아웃
                        initPrivacy(); // 개인정보 초기화
                        savePreference(); // 프리퍼런스에 저장

                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra("userID", userID);

                        finish();
                        startActivity(intent);
                        break;

                    case R.id.modify_member: //회원정보 수정
                        if (userID == null) {
                            final AlertDialog.Builder alertDialogBuilder3 = new AlertDialog.Builder(context);
                            alertDialogBuilder3.setMessage("로그인하시면 이용하실 수 있습니다.");
                            alertDialogBuilder3.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //todo
                                }
                            });
                            // 다이얼로그 생성
                            AlertDialog alertDialog3 = alertDialogBuilder3.create();
                            // 다이얼로그 보여주기
                            alertDialog3.show();
                            return;
                        } else {
                            startActivity(new Intent(context, ModifyActivity.class).putExtra("userID", userID).putExtra("userPassword", userPassword).putExtra("userName", userName).putExtra("userAge", userAge));
                        }
                        break;
                    case R.id.share:
                        shareKakao();
                        break;
                    case R.id.email:
                        startActivity(new Intent(context, MailActivity.class));
                        break;
                    case R.id.standard: //등급기준
                        show_standard();
                        break;
                    case R.id.setting: //환경설정
                        finish();
                        startActivity(new Intent(context, SettingActivity.class));
                        break;
                }
            }
        };
        lc_list.setOnClickListener(onClickListener);
        detail.setOnClickListener(onClickListener);
        floatingActionButton1.setOnClickListener(onClickListener);
        floatingActionButton2.setOnClickListener(onClickListener);
        floatingActionButton3.setOnClickListener(onClickListener);
        btn_login.setOnClickListener(onClickListener);
        btn_logout.setOnClickListener(onClickListener);
        modify_member.setOnClickListener(onClickListener);
        mail.setOnClickListener(onClickListener);
        share.setOnClickListener(onClickListener);
        standard.setOnClickListener(onClickListener);
        setting.setOnClickListener(onClickListener);
    }

    public void shareKakao() {
        //카카오 공유기능
        try {
            final KakaoLink kakaoLink = KakaoLink.getKakaoLink(this);
            final KakaoTalkLinkMessageBuilder kakaoBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

        /*메시지 추가*/
            kakaoBuilder.addText("서울시의 대기정보와 문화 정보를 한눈에 알고 싶다면?");

        /*이미지 가로/세로 사이즈는 80px 보다 커야하며, 이미지 용량은 500kb 이하로 제한된다.*/
            String url = "http://imageshack.com/a/img923/7427/nPChaK.jpg";
            kakaoBuilder.addImage(url, 160, 160);

        /*앱 실행버튼 추가*/
            kakaoBuilder.addAppButton("지금 시작하기");

        /*메시지 발송*/
            kakaoLink.sendMessage(kakaoBuilder, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //개인정보 프리퍼런스에 저장
    void savePreference() {
        SharedPreferences auto_privacy = getSharedPreferences("auto_privacy", MODE_PRIVATE);
        SharedPreferences.Editor ed_auto_privacy = auto_privacy.edit();

        ed_auto_privacy.putString("userID", userID);
        ed_auto_privacy.putString("userPassword", userPassword);
        ed_auto_privacy.putString("userName", userName);
        ed_auto_privacy.putInt("userAge", userAge);
        ed_auto_privacy.putInt("autoCheck", autoCheck);
        ed_auto_privacy.commit();
    }

    //개인정보 초기화
    private void initPrivacy() {
        userID = null;
        userPassword = null;
        userName = null;
        userAge = 0;
        autoCheck = 0;
    }

    //ArrayList index 불러오기
    private int getArrayIndex(int arrays, String findIndex) {
        String[] arrayString = getResources().getStringArray(arrays);
        for (int e = 0; e < arrayString.length; e++) {
            if (arrayString[e].equals(findIndex))
                return e;
        }
        return -1;
    }

    /* 특정시간 알림 */
    void Alarm() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, BroadcastD.class);
        PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();

        //알람 설정 (매일 오전 7시)
        if (calendar.get(Calendar.HOUR_OF_DAY) > 7) // 현재 시간이 7시 지났을때 다음날 7시부터 설정
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE) + 1, 7, 0, 0);
        else  // 안넘었을때는 당일 7시부터 설정
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 7, 0, 0);
        // 알람 반복 설정 => 24*60*60*1000 이건 하루에한번 계속 알람한다는 뜻
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, sender);
    }

    void Alarm_cancel() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, BroadcastD.class);
        PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, FLAG_UPDATE_CURRENT);

        am.cancel(sender);
    }

    /* 상세보기 다이얼로그 */
    void show_detail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_detail, null);
        builder.setView(view);

        typeMini = (CulturalEventTypeMini) view.findViewById(R.id.type_mini);
        typeMini.setOpenAPIKey(key);

        final TextView LOCAL = (TextView) view.findViewById(R.id.LOCAL);
        final TextView GRADE = (TextView) view.findViewById(R.id.GRADE);
        final TextView maxIndex = (TextView) view.findViewById(R.id.maxIndex);
        final TextView NITROGEN = (TextView) view.findViewById(R.id.NITROGEN);
        final TextView OZONE = (TextView) view.findViewById(R.id.OZONE);
        final TextView CARBON = (TextView) view.findViewById(R.id.CARBON);
        final TextView SULFUROUS = (TextView) view.findViewById(R.id.SULFUROUS);
        final TextView PM10 = (TextView) view.findViewById(R.id.PM10);
        final TextView PM25 = (TextView) view.findViewById(R.id.PM25);

        LOCAL.setText(element[0]);
        LOCAL.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        GRADE.setText(element[1]);
        maxIndex.setText(element[2] + "ppm");
        NITROGEN.setText(element[3] + "ppm");
        OZONE.setText(element[4] + "ppm");
        CARBON.setText(element[5] + "ppm");
        SULFUROUS.setText(element[6] + "ppm");
        PM10.setText(element[7] + "㎍/㎥");
        PM25.setText(element[8] + "㎍/㎥");

        final Button close = (Button) view.findViewById(R.id.buttonClose);
        final AlertDialog dialog = builder.create();

        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    void show_standard() {
        /* dialog_standard */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_standard, null);
        builder.setView(view);

        final Button cancle = (Button) view.findViewById(R.id.buttonCancle);
        final AlertDialog dialog = builder.create();

        dialog.show();

        cancle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    /* 로그인 다이얼로그 */
    void show_login() {
        /* dialog_login */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);
        builder.setView(view);

        final Button submit = (Button) view.findViewById(R.id.buttonSubmit);
        final Button register = (Button) view.findViewById(R.id.buttonRegister);
        final EditText login_idText = (EditText) view.findViewById(R.id.idText);
        final EditText login_passwordText = (EditText) view.findViewById(R.id.passwordText);
        final CheckBox autoLoginCheck = (CheckBox) view.findViewById(R.id.autoLoginCheck);
        final CheckBox saveID = (CheckBox) view.findViewById(R.id.saveID);

        SharedPreferences id = getSharedPreferences("save", MODE_PRIVATE);
        login_idText.setText(id.getString("userID", ""));
        saveID.setChecked(id.getBoolean("saveIDCheck", false));

        final AlertDialog dialog = builder.create();

        dialog.show();

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userID = login_idText.getText().toString();
                userPassword = login_passwordText.getText().toString();

                Response.Listener<String> responseListener = new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                dialog.dismiss();
                                Intent intent = new Intent(context, MainActivity.class);

                                userID = jsonResponse.getString("userID");
                                userPassword = jsonResponse.getString("userPassword");
                                userName = jsonResponse.getString("userName");
                                userAge = jsonResponse.getInt("userAge");

                                /* SharedPreference */
                                SharedPreferences id = getSharedPreferences("save", MODE_PRIVATE);
                                SharedPreferences.Editor saveID_info = id.edit();

                                //자동로그인 체크 이벤트
                                if (autoLoginCheck.isChecked()) {
                                    autoCheck++;
                                    savePreference();
                                } else {
                                    intent.putExtra("userID", userID);
                                    intent.putExtra("userPassword", userPassword);
                                    intent.putExtra("userName", userName);
                                    intent.putExtra("userAge", userAge);
                                }
                                if (saveID.isChecked()) {
                                    saveID_info.putString("userID", userID);
                                    saveID_info.putBoolean("saveIDCheck", true);
                                    saveID_info.commit();
                                } else {
                                    saveID_info.putString("userID", null);
                                    saveID_info.putBoolean("saveIDCheck", false);
                                    saveID_info.commit();
                                }

                                finish();
                                startActivity(intent);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage("로그인에 실패하였습니다.")
                                        .setNegativeButton("다시 시도", null)
                                        .create()
                                        .show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                LoginRequest loginRequest = new LoginRequest(userID, userPassword, responseListener);
                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(loginRequest);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(context, RegisterActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflater함수를 이용해서 menu 리소스를 menu로 변환.
        // 액션바 호출
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBar 메뉴 클릭에 대한 이벤트 처리
        String txt = null;
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
                if (!drawer.isDrawerOpen(Gravity.LEFT)) {
                    drawer.openDrawer(Gravity.LEFT);
                }
                break;
            case R.id.member:
                if (userID == null) show_login();

                else {
                    startActivity(new Intent(context, ModifyActivity.class)
                            .putExtra("userID", userID)
                            .putExtra("userPassword", userPassword)
                            .putExtra("userName", userName)
                            .putExtra("userAge", userAge));
                }
        }
        return super.onOptionsItemSelected(item);
    }

    //back 키 2번 종료
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - back_time >= 2000) {
            back_time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - back_time < 2000) {
            finish();
        }
    }

    private class GetXMLTask extends AsyncTask<String, Void, Document> {
        ProgressDialog asyncDialog = new ProgressDialog(
                MainActivity.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중입니다..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                url = new URL("http://openapi.seoul.go.kr:8088/" +
                        KEY + "/xml/ListAirQualityByDistrictService/1/25");
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); //XML문서 빌더 객체를 생성
                doc = db.parse(new InputSource(url.openStream())); //XML문서를 파싱한다.
                doc.getDocumentElement().normalize();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {
            String s = "";
            //row태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
            NodeList nodeList = doc.getElementsByTagName("row");
            //row 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환

            /*            pie Chart                 */
            pieChart = (PieChart) findViewById(R.id.piechart);
            ArrayList<PieEntry> yValues = new ArrayList<PieEntry>();

            pieChart.setUsePercentValues(false);
            pieChart.getDescription().setEnabled(false);
            pieChart.setExtraOffsets(5, 10, 5, 0);

            pieChart.setDragDecelerationFrictionCoef(0.95f);

            pieChart.setDrawHoleEnabled(true);
            pieChart.setTransparentCircleRadius(30f);
            pieChart.setHoleRadius(30f);
            pieChart.setHoleColor(Color.WHITE);

            Description description = new Description();
            description.setTextSize(18);
            pieChart.setDescription(description);
            pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic); //애니메이션

            PieDataSet dataSet = new PieDataSet(yValues, "Category");
            dataSet.setSliceSpace(10f);
            dataSet.setSelectionShift(10f); // pie chart size => high : ↓, low : ↑
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

            PieData data = new PieData((dataSet));
            data.setValueTextSize(18f);
            data.setValueTextColor(Color.DKGRAY);

            for (int i = 0; i < nodeList.getLength(); i++) {
                //날씨 데이터를 추출
                Node node = nodeList.item(i);//row엘리먼트 노드
                Element fstElmnt = (Element) node;
                int j = 0;

                //지역
                NodeList msrstenameList = fstElmnt.getElementsByTagName("MSRSTENAME");
                if (msrstenameList.item(0).getChildNodes().item(0).getNodeValue().equals(items[localnm])) {
                    element[j] = msrstenameList.item(0).getChildNodes().item(0).getNodeValue();
                    j++;
//                    yValues.add(new PieEntry(48f, elements[0]));

                    //gradeList 등급
                    NodeList gradeList = fstElmnt.getElementsByTagName("GRADE");
                    element[j] = gradeList.item(0).getChildNodes().item(0).getNodeValue();
                    j++;

                    //maxIndex 통합대기환경지수
                    NodeList maxIndex = fstElmnt.getElementsByTagName("MAXINDEX");
                    element[j] = maxIndex.item(0).getChildNodes().item(0).getNodeValue();
                    j++;
                    yValues.add(new PieEntry(Float.parseFloat(element[2]), "대기지수"));

                    //NITROGEN 이산화질소
                    NodeList nitrogenList = fstElmnt.getElementsByTagName("NITROGEN");
                    element[j] = nitrogenList.item(0).getChildNodes().item(0).getNodeValue();
                    j++;

                    //OZONE 오존
                    NodeList ozoneList = fstElmnt.getElementsByTagName("OZONE");
                    element[j] = ozoneList.item(0).getChildNodes().item(0).getNodeValue();
                    j++;

                    //CARBON 일산화탄소
                    NodeList carbonList = fstElmnt.getElementsByTagName("CARBON");
                    element[j] = carbonList.item(0).getChildNodes().item(0).getNodeValue();
                    j++;

                    //SULFUROUS	아황산가스
                    NodeList sulfurousList = fstElmnt.getElementsByTagName("SULFUROUS");
                    element[j] = sulfurousList.item(0).getChildNodes().item(0).getNodeValue();
                    j++;

                    //PM10 미세먼지
                    NodeList pm10List = fstElmnt.getElementsByTagName("PM10");
                    element[j] = pm10List.item(0).getChildNodes().item(0).getNodeValue();
                    if (!element[j].equals("점검중")) // 점검중이 아닐 때만 미세먼지 값 넣어주기
                        yValues.add(new PieEntry(Float.parseFloat(element[j]), "미세"));
                    j++;

                    //PM25 초미세먼지
                    NodeList pm25List = fstElmnt.getElementsByTagName("PM25");
                    element[j] = pm25List.item(0).getChildNodes().item(0).getNodeValue();
                    yValues.add(new PieEntry(Float.parseFloat(element[j]), "초미세"));
                }
            }
            description.setText(items[localnm] + " : " + element[1]); //라벨
            dataSet.setValueTextColor(Color.BLACK);
            pieChart.setData(data);
            pieChart.invalidate();
            asyncDialog.dismiss();
            super.onPostExecute(doc);
        }
    }

}