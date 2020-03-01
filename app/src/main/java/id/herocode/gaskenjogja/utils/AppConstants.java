package id.herocode.gaskenjogja.utils;

public class AppConstants {
    public class ServiceType {
        private static final String BASE_URL = "https://www.herocode.my.id/api/";
        public static final String LOGIN = BASE_URL + "login.php";
        public static final String REGISTER = BASE_URL + "daftar.php";
        public static final String DATA_WISATA = BASE_URL + "data_wisata.php";
        public static final String QRCODE_DATA = BASE_URL + "qrcodeapi.php";
        public static final String UPDATE_SALDO = BASE_URL + "updatesaldo.php";
        public static final String BOOKING = BASE_URL + "booking.php";
        public static final String LOKASI = BASE_URL + "android_update_lokasi.php";
        public static final String TRANSAKSI = BASE_URL + "riwayatTransaksi.php";
        public static final String LOKASI_TERDEKAT = BASE_URL + "android_lokasi_terdekat.php";
    }

    public class Params {
        public static final String NAME = "nama";
        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";
        public static final String SALDO = "saldo";
        public static final String ID_USER = "id_user";
        public static final String QRCODEDATA = "qrcode";
        public static final String TOPUP = "topup";
        public static final String BAYAR = "harga";
        public static final String ID_WISATA = "id_wisata";
        public static final String TGL_TRANSAKSI = "tanggal_transaksi";
        public static final String STATUS_WISATA = "status_wisata";
        public static final String RADIUS = "radius";
    }
}
