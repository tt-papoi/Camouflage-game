package com.example.camouflagegame.Activity;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.camouflagegame.R;

public class GuideActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private ImageButton btnEnglish, btnVietnamese;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.guide_screen);

        btnBack = findViewById(R.id.btnBack_guideScreen);
        btnEnglish = findViewById(R.id.btnEnglish);
        btnVietnamese = findViewById(R.id.btnVietnamese);
        WebView myWebView = findViewById(R.id.webview);
        myWebView.setBackgroundColor(Color.TRANSPARENT);

        String myHtmlStringEng = "<html><head><style>" +
                "body { text-align: justify; margin: 0; padding: 16px; font-size: 16px; color: #FFFFFF; line-height: 1.6; }" +
                "ul { list-style-type: disc; padding-left: 15px; }" +
                "li.first { list-style-type: disc; margin-bottom: 15px; }" +
                "li.second { margin-bottom: 8px; list-style-type: square;}" +
                "li.third { margin-bottom: 8px; list-style-type: none;}" +
                "</style></head><body>" +
                "<ul>" +
                "<p><li class='first'><strong>Introduction:</strong><br/>" +
                "CamouflageGame is played in a 1vs1 format, each player will be a commander owning tanks, aircraft, .. and military equipment of the same number and shape. The player's task is to hide that military equipment in a smart and unpredictable way on a battlefield simulated in the form of a chessboard with squares.</li>" +
                "<li class='first'><strong>How to play:</strong><br/>" +
                "+ Create Room - Create a room and wait for other players to connect to the room.<br/>" +
                "+ Join Room - Enter the room, find and connect to the device you want to play with.</li>" +
                "<li class='first'><strong>Fight:</strong>" +
                "<li class='second'>B1. Before starting to play, the player will choose one of the commanders he already owns to go into battle.</li>" +
                "<li class='second'>B2. At the beginning, players will have time to prepare, during which time players on both sides will arrange their military equipment..</li>" +
                "<li class='second'>B3. After the preparation phase is complete, players will take turns using firepower to shoot at any square on the board..</li>" +
                "<li class='third'>-> If the enemy's weapon hits, the player will be able to shoot again.<br/>" +
                "-> If you miss, you will have to give the shot to the opponent.<br/>" +
                "<li class='third'><em>*The condition for victory is to find and shoot all hidden weapons before the enemy does the same with our weapons..</li></em>" +

                "<li><strong>Explain:</strong><br/>"+
                "<em><strong>1) Types of weapons:</strong></em><br/>" +
                "Weapons (including: tanks, military aircraft, etc.) can be arranged in a straight or horizontal line with possible dimensions of 1x2, 1x3, 1x4, 1x5 to challenge players to experience..<br/><br/>" +
                "<em><strong>2) Types of commanders:</strong></em><br/>" +
                "Each type of commander has a special skill when going into battle, being able to bombard many cells with many different shapes, however it will consume energy. You need to use skills at the right time and smartly to optimize the battle.</p></body></html>";

        String myHtmlStringVie = "<html><head><style>" +
                "body { text-align: justify; margin: 0; padding: 16px; font-size: 16px; color: #FFFFFF; line-height: 1.6; }" +
                "ul { list-style-type: disc; padding-left: 15px; }" +
                "li.first { list-style-type: disc; margin-bottom: 15px; }" +
                "li.second { margin-bottom: 8px; list-style-type: square;}" +
                "li.third { margin-bottom: 8px; list-style-type: none;}" +
                "</style></head><body>" +
                "<ul>" +
                "<p><li class='first'><strong>Giới thiệu:</strong><br/>" +
                "CamouflageGame được chơi dưới hình thức 1vs1, mỗi người chơi sẽ là một chỉ huy sở hữu xe tăng, máy bay, .. và các khí tài quân sự có số lượng và hình dạng như nhau. Nhiệm vụ của người chơi là sẽ giấu số khí tài quân sự đó một cách thông minh và khó đoán trên chiến trường được mô phỏng dưới dạng bàn cờ với các ô vuông.</li>" +
                "<li class='first'><strong>Cách chơi:</strong><br/>" +
                "+ Create Room - Tạo phòng và đợi người chơi khác kết nối vào phòng.<br/>" +
                "+ Join Room - Vào phòng, tìm và kết nối đến thiết bị mình muốn chơi cùng.</li>" +
                "<li class='first'><strong>Chiến đấu:</strong>" +
                "<li class='second'>B1. Trước khi bắt đầu chơi, người chơi sẽ chọn cho mình một trong các vị chỉ huy mà mình đã sỡ hữu để ra trận.</li>" +
                "<li class='second'>B2. Khi bắt đầu, người chơi sẽ có thời gian để chuẩn bị, thời gian này người chơi hai bên sẽ tiến hành sắp xếp các khí tài quân sự của mình.</li>" +
                "<li class='second'>B3. Sau khi giai đoạn chuẩn bị đã hoàn tất, người chơi sẽ thay nhau dùng hỏa lực bắn vào từng ô một bất kì trên bàn đấu.</li>" +
                "<li class='third'>-> Nếu trúng khí tài kẻ địch, người chơi sẽ được bắn tiếp.<br/>" +
                "-> Nếu trượt sẽ phải nhường lượt bắn cho đối phương.<br/>" +
                "<li class='third'><em>*Điều kiện để chiến thắng là tìm ra và bắn được hết khí tài đang bị giấu, trước khi kẻ địch làm điều tương tự với khí tài của ta.</li></em>" +

                "<li><strong>Giải thích:</strong><br/>" +
                "<em><strong>1) Các loại khí tài:</strong></em><br/>" +
                "Các khí tài (bao gồm: xe tăng, máy bay quân sự, ..) có thể được sắp xếp nằm theo đường thẳng hoặc ngang với kích thước có thể là 1x2, 1x3, 1x4, 1x5 để thách thức người chơi trải nghiệm.<br/><br/>" +
                "<em><strong>2) Các loại chỉ huy:</strong></em><br/>" +
                "Mỗi loại chỉ huy có một loại kĩ năng đặc biệt khi ra trận, có thể bắn phá được nhiều ô với nhiều hình dạng khác nhau, tuy nhiên sẽ hao tốn năng lượng. Bạn cần sử dụng kĩ năng thật đúng lúc và thông minh để tối ưu cho cuộc chiến.</p></body></html>";

        myWebView.loadDataWithBaseURL(null, myHtmlStringEng, "text/html", "UTF-8", null);
        btnEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myWebView.loadDataWithBaseURL(null, myHtmlStringEng, "text/html", "UTF-8", null);
            }
        });
        btnVietnamese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myWebView.loadDataWithBaseURL(null, myHtmlStringVie, "text/html", "UTF-8", null);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}