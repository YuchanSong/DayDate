package uchan.weather.mail;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import uchan.weather.R;

//import kr.go.seoul.apiair.R;

public class MailActivity extends AppCompatActivity
{
    private GMailSender m;

    EditText et_content;
    EditText et_title;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);
        setupActionBar();

        Button btn_send = (Button) this.findViewById(R.id.btn_send);
        et_content = (EditText) findViewById(R.id.et_content);
        et_title = (EditText) findViewById(R.id.et_title);

        btn_send.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {

                GMailSender sender = new GMailSender("tpals24@gmail.com", "rcbawzrxjigzwyvr"); // SUBSTITUTE

                if (android.os.Build.VERSION.SDK_INT > 9)
                {

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();

                    StrictMode.setThreadPolicy(policy);

                }
                try
                {
                    sender.sendMail(et_title.getText().toString(), // subject.getText().toString(),
                            et_content.getText().toString(), // body.getText().toString(),
                            "tpals24@gmail.com", // from.getText().toString(),
                            "tpals24@gmail.com" // to.getText().toString()
                    );
                    toast();
                } catch (Exception e)
                {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("문의하기");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBar 메뉴 클릭에 대한 이벤트 처리
        String txt = null;
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toast()
    {
        Toast.makeText(this, "전송되었습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }
}