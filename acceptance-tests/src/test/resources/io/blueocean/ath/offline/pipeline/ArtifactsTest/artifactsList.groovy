node {
    // Create 150 files
    sh 'touch 0.txt 1.txt 2.txt 3.txt 4.txt 5.txt 6.txt 7.txt 8.txt 9.txt 10.txt 11.txt 12.txt 13.txt 14.txt 15.txt 16.txt 17.txt 18.txt 19.txt 20.txt 21.txt 22.txt 23.txt 24.txt 25.txt 26.txt 27.txt 28.txt 29.txt 30.txt 31.txt 32.txt 33.txt 34.txt 35.txt 36.txt 37.txt 38.txt 39.txt 40.txt 41.txt 42.txt 43.txt 44.txt 45.txt 46.txt 47.txt 48.txt 49.txt 50.txt 51.txt 52.txt 53.txt 54.txt 55.txt 56.txt 57.txt 58.txt 59.txt 60.txt 61.txt 62.txt 63.txt 64.txt 65.txt 66.txt 67.txt 68.txt 69.txt 70.txt 71.txt 72.txt 73.txt 74.txt 75.txt 76.txt 77.txt 78.txt 79.txt 80.txt 81.txt 82.txt 83.txt 84.txt 85.txt 86.txt 87.txt 88.txt 89.txt 90.txt 91.txt 92.txt 93.txt 94.txt 95.txt 96.txt 97.txt 98.txt 99.txt 100.txt 101.txt 102.txt 103.txt 104.txt 105.txt 106.txt 107.txt 108.txt 109.txt 110.txt 111.txt 112.txt 113.txt 114.txt 115.txt 116.txt 117.txt 118.txt 119.txt 120.txt 121.txt 122.txt 123.txt 124.txt 125.txt 126.txt 127.txt 128.txt 129.txt 130.txt 131.txt 132.txt 133.txt 134.txt 135.txt 136.txt 137.txt 138.txt 139.txt 140.txt 141.txt 142.txt 143.txt 144.txt 145.txt 146.txt 147.txt 148.txt 149.txt 150.txt'

    // Archive all files.
    archive '*'
}
