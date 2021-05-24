#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAXLINE 1000
#define FORMAT_TIME_LEN 10
#define SECOND_PER_MINUTE 60
#define BUTTON_ANI_SEC 3
#define ARG_COUNT 5

void trim_lrc(char* start_time_s, char* end_time_s, char* org_lrc_path, char* new_lrc_path);
void trans_time(double time_in_sec, char* format_time);

int main(int argc, char* argv[]) {

    if (argc != ARG_COUNT) {
        fprintf(stderr, "Wrong number of command line arguments.\n");
        exit(-1);
    }

    trim_lrc(argv[1], argv[2], argv[3], argv[4]);

    return 0;
}

void trim_lrc(char* start_time_s, char* end_time_s, char* org_lrc_path, char* new_lrc_path) {

    FILE* chord, * org_lrc, * new_lrc;
    double start_time, end_time;
    char format_start_time[FORMAT_TIME_LEN + 1];
    char format_end_time[FORMAT_TIME_LEN + 1];
    char append_head_time[FORMAT_TIME_LEN + 1];
    char buf[MAXLINE];

    sscanf(start_time_s, "%lf", &start_time);
    sscanf(end_time_s, "%lf", &end_time);

    if (start_time < BUTTON_ANI_SEC) {
        fprintf(stderr, "Start time cannot be less than button animation time.\n");
        exit(-1);
    }

    trans_time(start_time, format_start_time);
    trans_time(end_time, format_end_time);
    trans_time(start_time - BUTTON_ANI_SEC, append_head_time);

    /* Trim the original lrc file and write to the new one*/
    org_lrc = fopen(org_lrc_path, "r");
    if (org_lrc == NULL) {
        fprintf(stderr, "Cannot open the original lrc file.\n");
        exit(-1);
    }
    new_lrc = fopen(new_lrc_path, "w");
    if (new_lrc == NULL) {
        fprintf(stderr, "Cannot open the new lrc file.\n");
        exit(-1);
    }

    /* Before start time */
    while (fgets(buf, MAXLINE, org_lrc)) {
        if (strncmp(buf, format_start_time, FORMAT_TIME_LEN) >= 0) {
            fprintf(new_lrc, "%s\n", append_head_time);     // head empty line
            fputs(buf, new_lrc);        // first line with lyric
            break;
        }
    }

    /* Write the required part */
    while (fgets(buf, MAXLINE, org_lrc)) {
        if (strncmp(buf, format_end_time, FORMAT_TIME_LEN) > 0)
            break;
        fputs(buf, new_lrc);
    }

    /* Write the tail empty line */
    fprintf(new_lrc, "%s\n", format_end_time);

    fclose(org_lrc);
    fclose(new_lrc);

    fprintf(stdout, "Successfully trimmed the lrc file.\n");

}

void trans_time(double time_in_sec, char* format_time) {
    int minutes = 0;
    while (time_in_sec > SECOND_PER_MINUTE) {
        time_in_sec -= SECOND_PER_MINUTE;
        minutes++;
    }
    sprintf(format_time, "[%02d:%04.2f]", minutes, time_in_sec);
}