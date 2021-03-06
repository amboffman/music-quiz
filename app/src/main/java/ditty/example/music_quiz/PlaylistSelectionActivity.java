package ditty.example.music_quiz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.music.ditty.R;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.ListItems;

import java.util.ArrayList;

public class PlaylistSelectionActivity extends AppCompatActivity {
    private SpotifyAppRemote mSpotifyAppRemote;
    public static final String EXTRA_PLAYLIST_URI = "com.example.music_quiz.PLAYLISTURI";
    public static final String EXTRA_MODE= "com.example.music_quiz.MODE";
    private boolean endlessMode;
    GridView playlistGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_selection);
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        GradientBackground background = new GradientBackground(findViewById(R.id.playlistSelectionScreen));
        background.getGradientBackground();
        playlistGrid = findViewById(R.id.playlists);
        endlessMode = getIntent().getExtras().getBoolean(MainActivity.EXTRA_MESSAGE_MODE);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Connection connection = new Connection();
        // Set the connection parameters
        connection.connectSpotify(this, new ConnectionCallback() {
            @Override
            public void onSuccess() {
                Log.d("Quiz Connection:", "Successful");
                mSpotifyAppRemote = connection.spotifyRemote;
                fetchPlaylists();
            }

            @Override
            public void onError(String err) {
                Log.e("Quiz Connection:", "Failed");
                returnToHomeScreen();
            }
        });


    }
    private void returnToHomeScreen(){
        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
    }

    private void fetchPlaylists(){
        Playlist playlists = new Playlist();
        mSpotifyAppRemote.getContentApi().getRecommendedContentItems("DEFAULT")
                .setResultCallback(playlistRecommendations -> {
                    mSpotifyAppRemote.getContentApi().getChildrenOfItem(playlistRecommendations.items[0], 6, 0)
                            .setResultCallback(
                                    recentlyPlayedPlaylists -> {
                                        for(int i=0; i < recentlyPlayedPlaylists.total; i++){
                                            playlists.setPlaylists(recentlyPlayedPlaylists);
                                            playlists.addPlaylistUri(recentlyPlayedPlaylists.items[i].uri);
                                            mSpotifyAppRemote.getImagesApi().getImage(recentlyPlayedPlaylists.items[i].imageUri)
                                                    .setResultCallback(image->{
                                                        playlists.addPlaylistImage(image);
                                                        if(playlists.getPlaylistImages().size() == (recentlyPlayedPlaylists.total -1) ){
                                                            PlaylistAdapter playlistAdapter = new PlaylistAdapter(PlaylistSelectionActivity.this, R.layout.row_item, playlists.getPlaylists(), playlists.getPlaylistImages());
                                                            playlistGrid.setAdapter(playlistAdapter);
                                                            playlistGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                            @Override
                                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                             playPlaylist((String) playlists.getPlaylistUris().get(position));
                                                            }
                                                          });
                                                        }
                                                    });
                                        }

                                    }
                            );
                });

    }

    private void playPlaylist (String playlistURI){
        // Do something in response to button
        Intent gameActivity = new Intent(this, GameActivity.class);
        gameActivity.putExtra(EXTRA_PLAYLIST_URI, playlistURI);
        gameActivity.putExtra(EXTRA_MODE, endlessMode);
        startActivity(gameActivity);
    };


    @Override
    protected void onPause() {
        super.onPause();
        mSpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

}
