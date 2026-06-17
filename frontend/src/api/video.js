import request from '../utils/request'

export function getFeed(excludeIds = [], size = 10) {
  return request.get('/video/feed', {
    params: { excludeIds, size },
  })
}

export function getNextVideo(currentId, excludeIds = []) {
  return request.get(`/video/next/${currentId}`, {
    params: { excludeIds },
  })
}

export function getPrevVideo(currentId, excludeIds = []) {
  return request.get(`/video/prev/${currentId}`, {
    params: { excludeIds },
  })
}

export function likeVideo(videoId) {
  return request.post(`/video/like/${videoId}`)
}

export function unlikeVideo(videoId) {
  return request.post(`/video/unlike/${videoId}`)
}

export function publishVideo(file, title) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('title', title)
  return request.post('/video/publish', formData)
}

export function getMyVideos(page = 1, size = 10) {
  return request.get('/video/my', { params: { page, size } })
}

export function deleteVideo(id) {
  return request.delete(`/video/delete/${id}`)
}
