
SELECT 
    subject.pi_id, 
    subject_model.`name`,
    `state`.`name`, 
    FROM_UNIXTIME(subjecttrail.last_modified/1000),
    subjecttrail.last_modified,
    subject.u_id, 
    subjecttrail.id
FROM 
    subjecttrail
    JOIN subject ON subject.s_id = subjecttrail.subject
    JOIN subject_model ON subject_model.sm_id = subject.sm_id
    JOIN `state` ON subjecttrail.`state` = `state`.s_id
WHERE subject.pi_id = 1
ORDER BY subject.pi_id, subjecttrail.last_modified,subjecttrail.id ASC
