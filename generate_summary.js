const fs = require("fs");
const { Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType } = require("docx");

const doc = new Document({
    sections: [{
        properties: {},
        children: [
            new Paragraph({
                text: "FTPDroid - Session Changes Summary",
                heading: HeadingLevel.TITLE,
                alignment: AlignmentType.CENTER,
            }),
            new Paragraph({
                children: [
                    new TextRun("This document outlines in extreme detail every change made to the FTPDroid application during this session to fix UI/Backend desynchronization, progress tracking issues, and queue management bugs.")
                ],
            }),
            new Paragraph({
                text: "1. Data Synchronization & Event Emission (Backend)",
                heading: HeadingLevel.HEADING_1,
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "File: ", bold: true }),
                    new TextRun("app/src/main/kotlin/com/ftpdroid/app/data/network/ftp/FtpServerManager.kt"),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Issue: ", bold: true }),
                    new TextRun("The background FTP server (LoggingFtplet) was dropping transfer status events when the system was busy because it used a non-blocking `tryEmit` call on a limited buffer SharedFlow. This caused the UI to permanently miss \"TransferFinished\" signals, resulting in stuck transfers."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Fix: ", bold: true }),
                    new TextRun("Upgraded the event emission from `tryEmit` to a guaranteed `emit` inside a CoroutineScope. This ensures backpressure is handled and no events are lost."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Thread Safety: ", bold: true }),
                    new TextRun("Replaced standard `MutableMap` with `ConcurrentHashMap` for `startTimes` and `activeTransfers` to prevent concurrency crashes when multiple clients connect or transfer simultaneously."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Physical Path Resolution: ", bold: true }),
                    new TextRun("Modified the `STOR`, `APPE`, and `RETR` FTP commands to correctly resolve the physical absolute path of files on the device instead of relying on the virtual FTP path. This is crucial for the file size polling mechanism to find the file on disk."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Accurate Final Size: ", bold: true }),
                    new TextRun("Improved the `TransferFinished` collector. If the FTP session reports 0 bytes for an upload upon completion, the manager now falls back to checking the actual physical file size on the disk before saving the final record to the database."),
                ]
            }),

            new Paragraph({
                text: "2. Sequential Queue Processing & Speed Calculation (Backend)",
                heading: HeadingLevel.HEADING_1,
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "File: ", bold: true }),
                    new TextRun("app/src/main/kotlin/com/ftpdroid/app/service/TransferService.kt"),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Issue: ", bold: true }),
                    new TextRun("The app was attempting to execute all transfers in parallel. This caused network bottlenecks, corrupted the single `FtpClientManager` instance, and caused speed calculations to freeze at 0 B/s."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Sequential Queue Fix: ", bold: true }),
                    new TextRun("Completely refactored the service to use a sequential processing loop. It now uses a Coroutine `Mutex` and processes transfers one-by-one. Pending transfers sit in a \"QUEUED\" state until the active \"IN_PROGRESS\" transfer completes or fails."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Speed Calculation: ", bold: true }),
                    new TextRun("Replaced the hardcoded or inaccurate speed metrics with a delta-based calculation. The service now checks the bytes transferred every 1000ms (1 second), compares it to the previous value, and calculates the exact bytes-per-second speed, persisting this to the database."),
                ]
            }),

            new Paragraph({
                text: "3. Queue Ordering & Database Logic (Database)",
                heading: HeadingLevel.HEADING_1,
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "File: ", bold: true }),
                    new TextRun("app/src/main/kotlin/com/ftpdroid/app/data/local/db/dao/TransferDao.kt"),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Issue: ", bold: true }),
                    new TextRun("Files in the transfer queue would randomly swap positions due to unstable database sorting logic."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Fix: ", bold: true }),
                    new TextRun("Updated the `getPendingTransfers` SQL query to use deterministic sorting: `ORDER BY startedAt ASC, id ASC`. This ensures the queue is strictly FIFO (First-In-First-Out) and visually stable."),
                ]
            }),

            new Paragraph({
                text: "4. UI State Recycling & Visual Glitches (Frontend)",
                heading: HeadingLevel.HEADING_1,
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "File: ", bold: true }),
                    new TextRun("app/src/main/kotlin/com/ftpdroid/app/ui/screen/transfer/TransferQueueScreen.kt"),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Issue: ", bold: true }),
                    new TextRun("The \"100% progress thumb with 0% fill\" bug was caused by Jetpack Compose `LazyColumn` recycling state from old, completed list items into new, pending items."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Fix: ", bold: true }),
                    new TextRun("Added a unique identity key (`key = { it.id }`) to the `items` builder in the `LazyColumn`. This forces Compose to treat each transfer as a distinct element, preventing ghost animations and state bleeding."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• ETA Display: ", bold: true }),
                    new TextRun("Modified the ETA display logic. If the transfer speed is 0 B/s (e.g., during initialization or a stall), the UI now explicitly shows \"Calculating...\" instead of a blank space or crashing due to a divide-by-zero error."),
                ]
            }),

            new Paragraph({
                text: "5. SFTP Progress Tracking (Backend)",
                heading: HeadingLevel.HEADING_1,
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "File: ", bold: true }),
                    new TextRun("app/src/main/kotlin/com/ftpdroid/app/data/network/ftp/SftpClientManager.kt"),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Issue: ", bold: true }),
                    new TextRun("SFTP transfers had no progress tracking implemented."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Fix: ", bold: true }),
                    new TextRun("Implemented `net.schmizz.sshj.xfer.TransferListener` and `StreamCopier.Listener` during `uploadFile` and `downloadFile` operations to periodically report the transferred bytes to the UI."),
                ]
            }),

            new Paragraph({
                text: "6. FTP Client Stability (Backend)",
                heading: HeadingLevel.HEADING_1,
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "File: ", bold: true }),
                    new TextRun("app/src/main/kotlin/com/ftpdroid/app/data/network/ftp/FtpClientManager.kt"),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Issue: ", bold: true }),
                    new TextRun("Fetching file sizes after opening the data stream could cause the control connection to hang."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Fix: ", bold: true }),
                    new TextRun("Moved the `ftpClient.listFiles(remotePath)` call to retrieve the file size *before* the `retrieveFileStream` call is made in `downloadFile`."),
                ]
            }),

            new Paragraph({
                text: "7. Missing Value Placeholders (Frontend)",
                heading: HeadingLevel.HEADING_1,
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "Files: ", bold: true }),
                    new TextRun("TransferQueueViewModel.kt, TransferHistoryViewModel.kt"),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Issue: ", bold: true }),
                    new TextRun("Files sent to the phone via the local FTP Server showed \"Profile -1\" as the source."),
                ]
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "• Fix: ", bold: true }),
                    new TextRun("Added formatting logic to display \"Incoming (FTP Server)\" whenever the `profileId` is -1, making the origin of the file clear to the user."),
                ]
            }),
        ],
    }],
});

Packer.toBuffer(doc).then((buffer) => {
    const absolutePath = require('path').join(__dirname, "FTPDroid_Changes_Summary.docx");
    fs.writeFileSync(absolutePath, buffer);
    console.log("Successfully generated " + absolutePath);
});
