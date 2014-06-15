#ifndef _MICROHTTP_THREAD_
#define _MICROHTTP_THREAD_

#include <sys/types.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <microhttpd.h>

#define PORT 8080

#define MAC_AP1  "08:00:27:cb:c4:d7"
#define MAC_AP7  "08:00:27:91:fc:37"
#define MAC_AP11  "08:00:27:7d:3a:9f"


int microhttp_function(void);
int answer_to_connection (void *cls, struct MHD_Connection *connection,\
						const char *url, const char *method, const char *version,\
						const char *upload_data, size_t *upload_data_size, void **con_cls);

#endif
