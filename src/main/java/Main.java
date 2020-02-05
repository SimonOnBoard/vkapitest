import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.queries.friends.FriendsGetQuery;
import com.vk.api.sdk.queries.wall.WallGetQuery;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class Main {
    public static int count = 804;

    public static void main(String[] args) throws ClientException, ApiException, FileNotFoundException {
        String code = getCode();
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        UserAuthResponse authResponse = null;
        try {
            authResponse = vk.oauth()
                    .userAuthorizationCodeFlow(	7305584, "kmigddV53ayRawrw6Eiu", "https://oauth.vk.com/blank.html", code)
                    .execute();
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        } catch (ClientException e) {
            throw new IllegalStateException(e);
        }
        UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
        List<Integer> friends = getMyFriends(actor, vk);
        writeFriendList(friends);
        for(int i = 0; i < friends.size(); i++){
            System.out.println("Friend " + friends.get(i));
            getFriendsGroups(actor,vk,friends.get(i));
        }
        getMyGroupsInfo(actor, vk);
    }

    private static void getFriendsGroups(UserActor actor, VkApiClient vk, Integer friend) throws FileNotFoundException {
        com.vk.api.sdk.objects.groups.responses.GetResponse query1 = null;
        try {
            query1 = vk.groups().get(actor).userId(friend).count(1000).execute();
        } catch (ApiException e) {
            System.out.println(e);
        } catch (ClientException e) {
            throw new IllegalStateException(e);
        }
        if(query1 == null) {
            System.out.println(count);
            return;
        }
        List<Integer> groups = query1.getItems();
        PrintWriter printWriter = new PrintWriter(""+friend+".txt");
        System.out.println("--------------------------------------------------- groups   " + groups.size());
        for(int i = 0; i < groups.size();i++){
            getPostsToFile(vk,groups.get(i),printWriter,actor);
        }
        printWriter.close();
        System.out.println("----------------------------------------------------" + count + "===========================================");
    }

    private static void writeFriendList(List<Integer> friends) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("friends.txt"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        friends.stream().forEach(pw::println);
        pw.close();
    }

    private static List<Integer> getMyFriends(UserActor actor, VkApiClient vk) {
        FriendsGetQuery query = vk.friends().get(actor);
        GetResponse resp = null;
        try {
            resp = query.execute();
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        } catch (ClientException e) {
            throw new IllegalStateException(e);
        }
        return resp.getItems();
    }

    private static String getCode() {
        String token = "https://oauth.vk.com/authorize?client_id=7305584&display=page&redirect_uri=https://oauth.vk.com/blank.html&scope=friends&response_type=code&v=5.103";
        String code = "";
        try {
            code = askToken(token);
            System.out.println(code);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        return code;
    }

    public static String askToken(String link) throws IOException, URISyntaxException {
        Desktop.getDesktop().browse(new URI(link));
        return JOptionPane.showInputDialog("Please input access_token param from browser: ");
    }

    public static void getMyGroupsInfo(UserActor actor, VkApiClient vk) throws FileNotFoundException {
        com.vk.api.sdk.objects.groups.responses.GetResponse query1 = null;
        try {
            query1 = vk.groups().get(actor).count(1000).execute();
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        } catch (ClientException e) {
            throw new IllegalStateException(e);
        }
        List<Integer> groups = query1.getItems();
        PrintWriter printWriter = new PrintWriter("myGroupsText.txt");
        for(int i = 0; i < groups.size();i++){
            getPostsToFile(vk,groups.get(i),printWriter,actor);
        }
        printWriter.close();
        System.out.println(count);
    }

    private static void getPostsToFile(VkApiClient vk, Integer group, PrintWriter printWriter, UserActor actor) {
        count++;
        try {
            System.out.println(group);
            Thread.currentThread().sleep(1000);
            com.vk.api.sdk.objects.wall.responses.GetResponse query = vk.wall().get(actor).ownerId(-group).count(100).execute();
            List<WallPostFull> posts = query.getItems();
            for(int i = 0; i < posts.size();i++){
                String text = posts.get(i).getText();
                printWriter.println(text);
            }
            System.out.println("Posts size " + posts.size());
        } catch (ApiException e) {
            System.out.println(e);
        } catch (ClientException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    };
}
