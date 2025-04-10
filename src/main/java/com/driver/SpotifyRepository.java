package com.driver;

import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        for (Artist artist : artists) {
            if (artist.getName().equals(name)) return artist;
        }
        Artist artist = new Artist(name);
        artists.add(artist);
        artistAlbumMap.put(artist, new ArrayList<>());
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = createArtist(artistName);
        Album album = new Album(title);
        albums.add(album);
        artistAlbumMap.get(artist).add(album);
        albumSongMap.put(album, new ArrayList<>());
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = null;
        for (Album a : albums) {
            if (a.getTitle().equals(albumName)) {
                album = a;
                break;
            }
        }
        if (album == null) throw new Exception("Album does not exist");

        Song song = new Song(title, length);
        songs.add(song);
        albumSongMap.get(album).add(song);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = getUserByMobile(mobile);
        if (user == null) throw new Exception("User does not exist");
        Playlist playlist = new Playlist(title);
        List<Song> filteredSongs = new ArrayList<>();
        for (Song song : songs) {
            if (song.getLength() == length) {
                filteredSongs.add(song);
            }
        }
        playlistSongMap.put(playlist, filteredSongs);
        playlists.add(playlist);
        playlistListenerMap.put(playlist, new ArrayList<>(Collections.singletonList(user)));
        creatorPlaylistMap.put(user, playlist);
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = getUserByMobile(mobile);
        if (user == null) throw new Exception("User does not exist");
        Playlist playlist = new Playlist(title);
        List<Song> filteredSongs = new ArrayList<>();
        for (String songTitle : songTitles) {
            for (Song song : songs) {
                if (song.getTitle().equals(songTitle)) {
                    filteredSongs.add(song);
                    break;
                }
            }
        }
        playlistSongMap.put(playlist, filteredSongs);
        playlists.add(playlist);
        playlistListenerMap.put(playlist, new ArrayList<>(Collections.singletonList(user)));
        creatorPlaylistMap.put(user, playlist);
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = getUserByMobile(mobile);
        if (user == null) throw new Exception("User does not exist");
        Playlist playlist = null;
        for (Playlist p : playlists) {
            if (p.getTitle().equals(playlistTitle)) {
                playlist = p;
                break;
            }
        }
        if (playlist == null) throw new Exception("Playlist does not exist");
        if (creatorPlaylistMap.get(user) != playlist &&
                (playlistListenerMap.get(playlist) == null || !playlistListenerMap.get(playlist).contains(user))) {
            playlistListenerMap.get(playlist).add(user);
            userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        }
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = getUserByMobile(mobile);
        if (user == null) throw new Exception("User does not exist");
        Song likedSong = null;
        for (Song song : songs) {
            if (song.getTitle().equals(songTitle)) {
                likedSong = song;
                break;
            }
        }
        if (likedSong == null) throw new Exception("Song does not exist");
        List<User> likers = songLikeMap.computeIfAbsent(likedSong, k -> new ArrayList<>());
        if (!likers.contains(user)) {
            likers.add(user);
            likedSong.setLikes(likedSong.getLikes() + 1);
            // Auto-like artist
            for (Album album : albumSongMap.keySet()) {
                if (albumSongMap.get(album).contains(likedSong)) {
                    for (Artist artist : artistAlbumMap.keySet()) {
                        if (artistAlbumMap.get(artist).contains(album)) {
                            artist.setLikes(artist.getLikes() + 1);
                        }
                    }
                }
            }
        }
        return likedSong;
    }

    public String mostPopularArtist() {
        Artist result = null;
        int maxLikes = 0;
        for (Artist artist : artists) {
            if (artist.getLikes() > maxLikes) {
                maxLikes = artist.getLikes();
                result = artist;
            }
        }
        return result != null ? result.getName() : null;
    }

    public String mostPopularSong() {
        Song result = null;
        int maxLikes = 0;
        for (Song song : songs) {
            if (song.getLikes() > maxLikes) {
                maxLikes = song.getLikes();
                result = song;
            }
        }
        return result != null ? result.getTitle() : null;
    }

    private User getUserByMobile(String mobile) {
        for (User user : users) {
            if (user.getMobile().equals(mobile)) {
                return user;
            }
        }
        return null;
    }
}