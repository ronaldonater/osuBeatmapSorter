# osu! Mapset Organizer

A Java utility for organizing osu! beatmap practice collections by separating each difficulty into its own folder with its associated audio and background files.

## Overview

osu! Mapset Organizer is designed for osu! players who want to organize large practice mapset collections. When you have a single folder containing multiple difficulties of various beatmaps, this tool will:

1. Parse all `.osu` files in a directory
2. Extract metadata (artist, title, difficulty name)
3. Create individual folders for each difficulty
4. Copy the relevant `.osu` file, audio file, and background image to the appropriate folder

This makes it easier to navigate through your practice collection and find specific difficulties you want to play.

## Features

- Automatically organizes beatmaps based on difficulty
- Creates clean folder names using song metadata
- Preserves original files (copies rather than moves)
- Handles missing backgrounds or audio files gracefully
- Works with standard osu! beatmap format

## Requirements

- Java Runtime Environment (JRE) 8 or higher
- osu! beatmap files (.osu format)

## Installation

1. Clone this repository:
   ```
   git clone https://github.com/yourusername/osu-mapset-organizer.git
   ```

2. Navigate to the project directory:
   ```
   cd osu-mapset-organizer
   ```

3. Compile the Java file:
   ```
   javac OsuMapsetOrganizer.java
   ```

## Usage

### Basic Usage

Run the program with the path to your mapset folder as an argument:

```
java OsuMapsetOrganizer /path/to/your/mapset/folder
```

### Example

Let's say you have a folder called `practice_maps` with multiple beatmap difficulties:

```
java OsuMapsetOrganizer C:\Users\YourName\osu!\Songs\practice_maps
```

The program will:
1. Scan the folder for .osu files
2. Create a new folder for each difficulty
3. Copy the relevant files to each folder
4. Report the actions taken in the console output

### Output Example

```
Analyzing mapset folder: C:\Users\YourName\osu!\Songs\practice_maps
Found 5 .osu files. Analyzing...
Created folder: Camellia - Exit This Earth's Atomosphere [Another]
  - Copied difficulty: Camellia - Exit This Earth's Atomosphere (Another).osu
  - Copied audio: audio.mp3
  - Copied background: bg.jpg
Created folder: Camellia - Exit This Earth's Atomosphere [Extreme]
  - Copied difficulty: Camellia - Exit This Earth's Atomosphere (Extreme).osu
  - Copied audio: audio.mp3
  - Copied background: bg.jpg
...
Organization complete!
```

## Notes

- The program does not modify or delete your original files.
- If a beatmap is missing information (like an audio file reference), it will be skipped.
- Folder names are sanitized to remove characters that are invalid for file systems.
- For best results, ensure your .osu files follow the standard format with proper metadata.

## Contributing

Contributions are welcome! Feel free to submit issues or pull requests.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
