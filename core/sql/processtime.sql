
SELECT 
    MIN(process_instance.start_time) start_time,
    MAX(process_instance.end_time) endtime,
    MAX(process_instance.end_time) - MIN(process_instance.start_time) duration,    
    COUNT(process_instance.pi_id)
FROM 
    process_instance
;

SELECT 
    process_instance.start_time start_time,
    process_instance.end_time endtime,
    process_instance.end_time - process_instance.start_time duration,    
    process_instance.pi_id
FROM 
    process_instance
ORDER BY duration DESC
;



