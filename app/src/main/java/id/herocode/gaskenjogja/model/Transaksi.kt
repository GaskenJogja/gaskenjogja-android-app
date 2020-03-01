package id.herocode.gaskenjogja.model

class Transaksi {
    var idTransaksi: Int? = null
    var tanggalWisata: String? = null
    var statusWisata: String? = null
    var metode: String? = null
    var namaWisata: String? = null

    constructor() : super() {}

    constructor(
        idTransaksi: Int,
        tanggalWisata: String,
        statusWisata: String,
        metode: String,
        namaWisata: String
    ) {
        this.idTransaksi = idTransaksi
        this.tanggalWisata = tanggalWisata
        this.statusWisata = statusWisata
        this.metode = metode
        this.namaWisata = namaWisata
    }

}