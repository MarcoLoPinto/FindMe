package com.liner.findme.worker

import com.liner.findme.repositories.PhotoCardRepository

data class PhotoCardWorkerDependency(internal val photoCardRepository: PhotoCardRepository)
