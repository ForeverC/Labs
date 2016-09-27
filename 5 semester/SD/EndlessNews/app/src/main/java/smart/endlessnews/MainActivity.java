package smart.endlessnews;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    final int MENU_ADD_ID = 1;
    final int MENU_DELETE_ID = 2;
    final int MENU_QUIT_ID = 3;

    private SharedPreferences sharedPreferences;

    ArrayList<Category> categories = new ArrayList<>();
    ArrayList<News> allNews = new ArrayList<>();
    CategoryAdapter categoryAdapter;

    ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        if (savedInstanceState != null) {
            int categoriesCount = savedInstanceState.getInt("categories_count");
            int newsCount = savedInstanceState.getInt("news_count");
            for (int i = 0; i < categoriesCount; i++)
                categories.add((Category) savedInstanceState.getParcelable("category" + i));
            for (int i = 0; i < newsCount; i++)
                allNews.add((News) savedInstanceState.getParcelable("news" + i));
        } else {
            RSSParser rssParser = new RSSParser("http://www.vesti.ru/vesti.rss");
            allNews = rssParser.parseFeed();
            Toast.makeText(getApplicationContext(),
                    "News count: " + allNews.size(),
                    Toast.LENGTH_SHORT).show();
        }

        categoryAdapter = new CategoryAdapter(this, categories);

        lvMain = (ListView) findViewById(R.id.lvMain);

        if (lvMain != null)
            lvMain.setAdapter(categoryAdapter);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("categories_count", categories.size());
        outState.putInt("news_count", allNews.size());
        for (int i = 0; i < categories.size(); i++)
            outState.putParcelable("category" + i, categories.get(i));
        for (int i = 0; i < allNews.size(); i++)
            outState.putParcelable("news" + i, allNews.get(i));
    }

    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int size = categories.size();
        editor.putInt("categoriesCount", size);
        for (int i = 0; i < size; i++)
            editor.putString("categoryName" + i, categories.get(i).getName());
        editor.apply();
    }

    protected void onResume() {
        super.onResume();
        int size = sharedPreferences.getInt("categoriesCount", 0);
        if (size > 0 && categories.size() == 0)
            for (int i = 0; i < size; i++) {
                String name = sharedPreferences.getString("categoryName" + i, "");
                categories.add(new Category(name));
            }
        //else addTestCategories();
    }

    public void addTestCategories() {
        for (int i = 0; i < 3; i++)
            categories.add(new Category("Category " + (i + 1)));
        addTestNews(0);
    }

    public void addTestNews(int categoryIndex) {
        News news = new News("This is my first news's title!",
                "This news is goddamn useless! But I hope that someday I will finish RSS" +
                        " parser and news will be loaded automatically from news-websites!!!!!!!",
                "Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now. Full text is unavailable now.",
                "Link is unavailable now.",
                "http://scoopak.com/wp-content/uploads/2013/06/free-hd-natural-wallpapers-download-for-pc.jpg",
                categories.get(categoryIndex).getName(),
                new Date(100));

        categories.get(0).addNews(news);

        news = new News("This is my second news's title!",
                "This news is goddamn useless! But I hope that someday I will finish RSS" +
                        " parser and news will be loaded automatically from news-websites!!!!!!!",
                "Full text is unavailable now.",
                "Link is unavailable now.",
                "http://scoopak.com/wp-content/uploads/2013/06/free-hd-natural-wallpapers-download-for-pc.jpg",
                categories.get(categoryIndex).getName(),
                new Date(100));

        categories.get(0).addNews(news);
    }

    public boolean isNeededDeletion() {
        for (Category c : categories)
            if (c.getDeleteState()) return true;
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD_ID, 0, "Add new category");
        menu.add(0, MENU_DELETE_ID, 0, "Delete selected categories");
        menu.add(0, MENU_QUIT_ID, 0, "Quit");
        return super.onCreateOptionsMenu(menu);
    }

    public void addNewCategory() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("New category");
        alert.setMessage("Enter new category name:");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int btnID) {
                String value = input.getText().toString();
                if (value.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "Name can't be empty!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                categories = categoryAdapter.addCategory(value, allNews);
                categoryAdapter.notifyDataSetChanged();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int btnID) {
            }
        });

        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_ID:
                addNewCategory();
                break;
            case MENU_DELETE_ID:
                if (isNeededDeletion()) {
                    categories = categoryAdapter.deleteCategory();
                    categoryAdapter.notifyDataSetChanged();
                }
                break;
            case MENU_QUIT_ID:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
